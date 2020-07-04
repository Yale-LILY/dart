/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.StringTokenizer;

import edu.cmu.meteor.scorer.MeteorConfiguration;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import edu.cmu.meteor.util.Constants;

public class Trainer {

	// Defaults
	public static final double[] INITIAL = { 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0,
			0.0 };
	public static final double[] FINAL = { 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0 };
	public static final double[] STEP = { 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
			0.05, 0.05 };

	private static final double e = 0.001;

	private static final DecimalFormat df = new DecimalFormat("0.00");

	// Variables
	private static ArrayList<Double> initialWeights;
	private static ArrayList<Double> finalWeights;
	private static ArrayList<Double> step;

	private static ArrayList<MeteorStats> statsList;
	private static ArrayList<Double> terList;
	private static ArrayList<Double> lengthList;

	private static ArrayList<ArrayList<Integer>> gtList;
	private static ArrayList<Double> gtWeightList;

	private static double eps = 0.0;
	private static String language = "en";

	private static MeteorConfiguration config;
	private static ArrayList<Double> weights;

	private static PrintStream out = new PrintStream(System.out, false);

	public static void main(String[] args) {

		// Usage
		if (args.length < 2) {
			System.out.println("Meteor Trainer version " + Constants.VERSION);
			System.out
					.println("Usage: java -XX:+UseCompressedOops -Xmx2G -cp meteor-*.jar Trainer "
							+ "<task> <dataDir> [options]");
			System.out.println();
			System.out.println("Tasks:\t\t\t\tOne of: segcor spearman rank");
			System.out.println();
			System.out.println("Options:");
			System.out.println("-a paraphrase");
			System.out.println("-e epsilon");
			System.out.println("-l language");
			System.out.println("-ch\t\t\t\tfor character-based P and R");
			System.out
					.println("-noNorm\t\t\t\tdon't normalize, sgm files are pre-tokenized");
			System.out
					.println("-multi\t\t\t\tmulti-language.  Use noNorm and language-specific words/paraphrases");
			System.out.println("-i 'p1 p2 p3 p4 w1 w2 w3 w4'\tInitial "
					+ "parameters and weights");
			System.out.println("-f 'p1 p2 p3 p4 w1 w2 w3 w4'\tFinal "
					+ "parameters and weights");
			System.out.println("-s 'p1 p2 p3 p4 w1 w2 w3 w4'\tSteps");
			return;
		}

		String task = args[0];
		String dataDir = args[1];
		String paraFile = "";
		boolean charBased = false;
		boolean noNorm = false;
		boolean multi = true;

		// Load defaults
		initialWeights = new ArrayList<Double>();
		for (double n : INITIAL)
			initialWeights.add(n);
		finalWeights = new ArrayList<Double>();
		for (double n : FINAL)
			finalWeights.add(n);
		step = new ArrayList<Double>();
		for (double n : STEP)
			step.add(n);

		// Input args
		int curArg = 2;
		while (curArg < args.length) {
			if (args[curArg].equals("-i")) {
				initialWeights = makePaddedList(args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-f")) {
				finalWeights = makePaddedList(args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-s")) {
				step = makePaddedList(args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-a")) {
				paraFile = args[curArg + 1];
				curArg += 2;
			} else if (args[curArg].equals("-e")) {
				eps = Double.parseDouble(args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-l")) {
				language = args[curArg + 1];
				curArg += 2;
			} else if (args[curArg].equals("-ch")) {
				charBased = true;
				curArg += 1;
			} else if (args[curArg].equals("-noNorm")) {
				noNorm = true;
				curArg += 1;
			} else if (args[curArg].equals("-multi")) {
				noNorm = true;
				multi = true;
				curArg += 1;
			} else {
				System.err.println("Unknown option \"" + args[curArg] + "\"");
				System.exit(1);
			}
		}

		// Default paraphrase table for language if not specified
		if (paraFile.equals(""))
			paraFile = Constants.getDefaultParaFileURL(
					Constants.getLanguageID(Constants
							.normLanguageName(language))).getFile();

		// Add a value less than one step to account for Java double accuracy
		// issues
		for (int i = 0; i < finalWeights.size(); i++)
			finalWeights.set(i, finalWeights.get(i) + e);

		// Task
		if (task.equals("segcor")) {
			segcor(dataDir, paraFile, charBased, noNorm, false);
		} else if (task.equals("spearman")) {
			segcor(dataDir, paraFile, charBased, noNorm, true);
		} else if (task.equals("rank")) {
			rank(dataDir, paraFile, charBased, noNorm, multi);
		} else {
			System.err.println("Please specify a valid task");
			System.exit(1);
		}

	}

	private static void segcor(String dataDir, String paraFile,
			boolean charBased, boolean noNorm, boolean spearman) {
		/*
		 * Run Meteor on each available set and collect the sufficient
		 * statistics for rescoring. Create the MeteorStats list and the TER
		 * list in the same order to avoid lookups on doc/seg IDs.
		 */

		statsList = new ArrayList<MeteorStats>();
		terList = new ArrayList<Double>();
		lengthList = new ArrayList<Double>();

		File dataDirFile = new File(dataDir);
		String testFile = "";
		String refFile = "";

		for (String terFile : dataDirFile.list()) {
			// For each set
			if (terFile.endsWith(".ter")) {
				String sysName = terFile.split("\\.")[0];
				System.err.println(sysName);
				testFile = dataDir + "/" + sysName + ".tst";
				refFile = dataDir + "/" + sysName + ".ref";

				// Read the TER file
				Hashtable<String, Double> terTable = new Hashtable<String, Double>();
				try {
					BufferedReader in = new BufferedReader(new FileReader(
							dataDir + "/" + terFile));
					String line;
					while ((line = in.readLine()) != null) {
						StringTokenizer tok = new StringTokenizer(line);
						String doc = tok.nextToken();
						String seg = tok.nextToken();
						double ter = Double.parseDouble(tok.nextToken());
						terTable.put(doc + ":" + seg, ter);
					}
					in.close();
				} catch (FileNotFoundException ex) {
					System.err.println("Error: If you are viewing this error "
							+ "message, please check your filesystem and "
							+ "Java installation.");
					System.exit(1);
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(1);
				}

				Meteor.main(getMArgs(testFile, refFile, paraFile, charBased,
						noNorm, false));

				// Store the MeteorStats
				try {
					BufferedReader in = new BufferedReader(new FileReader(
							"meteor-seg.scr"));
					String line;
					while ((line = in.readLine()) != null) {
						StringTokenizer tok = new StringTokenizer(line, "\t");
						tok.nextToken(); // set
						tok.nextToken(); // sysName
						String doc = tok.nextToken(); // doc
						String seg = tok.nextToken(); // seg
						// stats
						String ss = tok.nextToken();
						MeteorStats stats = new MeteorStats(ss);
						// store stats
						statsList.add(stats);
						// store ter for same segment
						double ter = terTable.get(doc + ":" + seg);
						terList.add(ter);
						// store reference length
						lengthList.add((double) stats.referenceLength);
					}
					in.close();

					// Cleanup
					new File("meteor-seg.scr").delete();
					new File("meteor-doc.scr").delete();
					new File("meteor-sys.scr").delete();
				} catch (FileNotFoundException ex) {
					System.err.println("Error: System name and file name do "
							+ "not match for \"" + sysName + "\"");
					System.exit(1);
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}

		/*
		 * Rescore the MeteorStats using different parameters and record
		 * correlation with TER scores
		 */

		// Create configuration
		config = new MeteorConfiguration();
		config.setCharBased(charBased);
		ArrayList<Integer> none = new ArrayList<Integer>();
		config.setModules(none);
		weights = new ArrayList<Double>(initialWeights);

		int param = 0;
		rescore(param, spearman);
	}

	private static void rescore(int param, boolean spearman) {
		// Rescore if all weights specified
		if (param == step.size()) {

			ArrayList<Double> p = new ArrayList<Double>();
			p.add(weights.get(0));
			p.add(weights.get(1));
			p.add(weights.get(2));
			p.add(weights.get(3));

			ArrayList<Double> w = new ArrayList<Double>();
			w.add(weights.get(4));
			w.add(weights.get(5));
			w.add(weights.get(6));
			w.add(weights.get(7));

			config.setParameters(p);
			config.setModuleWeights(w);

			MeteorScorer scorer = new MeteorScorer(config);

			ArrayList<Double> meteorScore = new ArrayList<Double>();

			for (int seg = 0; seg < statsList.size(); seg++) {
				MeteorStats stats = statsList.get(seg);
				scorer.computeMetrics(stats);
				meteorScore.add(stats.score);
			}

			double correlation = spearman ? spearman(meteorScore, terList)
					: pearsonWeighted(meteorScore, terList, lengthList);

			out.print(correlation);
			for (Double n : weights)
				out.print(" " + df.format(n));
			out.println();
			return;
		}

		for (double n = initialWeights.get(param); n <= finalWeights.get(param); n += step
				.get(param)) {
			weights.set(param, n);
			rescore(param + 1, spearman);
		}
	}

	private static double spearman(ArrayList<Double> x, ArrayList<Double> y) {

		int N = x.size();
		double[][] xy = new double[N][];

		for (int i = 0; i < N; i++) {
			xy[i] = new double[] { x.get(i), y.get(i) };
		}
		// Rank X
		Arrays.sort(xy, new xyComparatorX());
		rankArray(xy, 0);
		// Rank Y
		Arrays.sort(xy, new xyComparatorY());
		rankArray(xy, 1);
		// Rank correlation
		return pearson(xy);
	}

	private static void rankArray(double[][] xy, int idx) {
		double sum = 0;
		int count = 0;
		for (int i = 0; i < xy.length; i++) {
			sum += (i + 1);
			count += 1;
			if (i == xy.length - 1 || xy[i][idx] != xy[i + 1][idx]) {
				for (int j = 0; j < count; j++) {
					xy[i - j][idx] = (sum / count);
				}
				sum = 0;
				count = 0;
			}
		}
	}

	private static class xyComparatorX implements Comparator<double[]> {
		public int compare(double[] o1, double[] o2) {
			return Double.compare(o1[0], o2[0]);
		}
	}

	private static class xyComparatorY implements Comparator<double[]> {
		public int compare(double[] o1, double[] o2) {
			return Double.compare(o1[1], o2[1]);
		}
	}

	private static double pearson(double[][] xy) {

		int N = xy.length;

		double sum_x = 0.0;
		double sum_y = 0.0;

		for (int i = 0; i < N; i++) {
			sum_x += xy[i][0];
			sum_y += xy[i][1];
		}

		double mean_x = (sum_x / N);
		double mean_y = (sum_y / N);

		double cov_x_y_top = 0.0;
		double cov_x_x_top = 0.0;
		double cov_y_y_top = 0.0;

		for (int i = 0; i < N; i++) {
			cov_x_y_top += ((xy[i][0] - mean_x) * (xy[i][1] - mean_y));
			cov_x_x_top += ((xy[i][0] - mean_x) * (xy[i][0] - mean_x));
			cov_y_y_top += ((xy[i][1] - mean_y) * (xy[i][1] - mean_y));
		}

		double cov_x_y = cov_x_y_top / N;
		double cov_x_x = cov_x_x_top / N;
		double cov_y_y = cov_y_y_top / N;

		double corr_pearson = cov_x_y / Math.sqrt(cov_x_x * cov_y_y);
		if (Double.isNaN(corr_pearson))
			return 0.0;
		return corr_pearson;
	}

	private static double pearsonWeighted(ArrayList<Double> x,
			ArrayList<Double> y, ArrayList<Double> w) {

		int N = w.size();

		double sum_x_w = 0.0;
		double sum_y_w = 0.0;
		double sum_w = 0.0;

		for (int i = 0; i < N; i++) {
			sum_x_w += (x.get(i) * w.get(i));
			sum_y_w += (y.get(i) * w.get(i));
			sum_w += w.get(i);
		}

		double mean_x = (sum_x_w / sum_w);
		double mean_y = (sum_y_w / sum_w);

		double cov_x_y_top = 0.0;
		double cov_x_x_top = 0.0;
		double cov_y_y_top = 0.0;

		for (int i = 0; i < N; i++) {
			cov_x_y_top += (w.get(i) * (x.get(i) - mean_x) * (y.get(i) - mean_y));
			cov_x_x_top += (w.get(i) * (x.get(i) - mean_x) * (x.get(i) - mean_x));
			cov_y_y_top += (w.get(i) * (y.get(i) - mean_y) * (y.get(i) - mean_y));
		}

		double cov_x_y = cov_x_y_top / sum_w;
		double cov_x_x = cov_x_x_top / sum_w;
		double cov_y_y = cov_y_y_top / sum_w;

		double corr_pearson = cov_x_y / Math.sqrt(cov_x_x * cov_y_y);
		if (Double.isNaN(corr_pearson))
			return 0.0;
		return corr_pearson;
	}

	private static void rank(String dataDir, String paraFile,
			boolean charBased, boolean noNorm, boolean multi) {
		/*
		 * Run Meteor on each available set and collect the sufficient
		 * statistics for rescoring. Create the MeteorStats list and the TER
		 * list in the same order to avoid lookups on doc/seg IDs.
		 */

		statsList = new ArrayList<MeteorStats>();
		lengthList = new ArrayList<Double>();

		Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> segIdx = new Hashtable<String, Hashtable<String, Hashtable<String, Integer>>>();
		int nextIdx = 0;

		String refFile = "";
		String langPair = "";
		String sysName = "";

		File dataDirFile = new File(dataDir);
		for (String tstFile : dataDirFile.list()) {
			// Skip rank and ref files
			if (tstFile.endsWith(".rank") || tstFile.endsWith(".ref.sgm"))
				continue;
			// ex: fr~1-en.system.sgm -> fr~1-en.ref.sgm
			refFile = tstFile.substring(0, tstFile.indexOf(".")) + ".ref.sgm";
			langPair = tstFile.substring(0, tstFile.indexOf("."));
			sysName = tstFile.substring(tstFile.indexOf(".") + 1,
					tstFile.lastIndexOf("."));

			// Run Meteor with all modules
			String test = dataDir + "/" + tstFile;
			String ref = dataDir + "/" + refFile;
			String[] mArgs = getMArgs(test, ref, paraFile, charBased, noNorm,
					multi);

			System.err.println(Arrays.toString(mArgs));
			Meteor.main(mArgs);

			if (!segIdx.containsKey(langPair))
				segIdx.put(langPair,
						new Hashtable<String, Hashtable<String, Integer>>());
			if (!segIdx.get(langPair).containsKey(sysName))
				segIdx.get(langPair).put(sysName,
						new Hashtable<String, Integer>());

			// Store the MeteorStats
			try {
				BufferedReader in = new BufferedReader(new FileReader(
						"meteor-seg.scr"));
				String line;
				while ((line = in.readLine()) != null) {
					StringTokenizer tok = new StringTokenizer(line, "\t");
					tok.nextToken(); // set
					tok.nextToken(); // sysName
					tok.nextToken(); // doc
					String seg = tok.nextToken(); // seg
					// stats
					String ss = tok.nextToken();
					MeteorStats stats = new MeteorStats(ss);
					// store stats & update segIdx
					statsList.add(stats);
					segIdx.get(langPair).get(sysName).put(seg, nextIdx);
					nextIdx++;
					// store reference length
					lengthList.add((double) stats.referenceLength);
				}
				in.close();

				// Cleanup
				new File("meteor-seg.scr").delete();
				new File("meteor-doc.scr").delete();
				new File("meteor-sys.scr").delete();
			} catch (FileNotFoundException ex) {
				System.err.println("Error: System name and file name do "
						+ "not match for \"" + sysName + "\"");
				System.exit(1);
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}

		}

		/*
		 * Load rank judgments
		 */

		gtList = new ArrayList<ArrayList<Integer>>();
		gtWeightList = new ArrayList<Double>();

		for (String rankFile : dataDirFile.list()) {
			if (!rankFile.endsWith(".rank"))
				continue;
			System.err.println(rankFile);
			try {
				BufferedReader in = new BufferedReader(new FileReader(dataDir
						+ "/" + rankFile));
				String line = "";
				// Greater-than
				while ((line = in.readLine()) != null) {
					StringTokenizer tok = new StringTokenizer(line, "\t");
					String seg = tok.nextToken();
					String lp1 = tok.nextToken();
					String sys1 = tok.nextToken();
					String lp2 = tok.nextToken();
					String sys2 = tok.nextToken();
					if (!segIdx.get(lp1).containsKey(sys1)
							|| !segIdx.get(lp2).containsKey(sys2))
						continue;
					// Create rank pair
					int idx1 = segIdx.get(lp1).get(sys1).get(seg);
					int idx2 = segIdx.get(lp2).get(sys2).get(seg);
					double weight = (lengthList.get(idx1) + lengthList
							.get(idx2)) / 2;
					ArrayList<Integer> pair = new ArrayList<Integer>();
					pair.add(idx1);
					pair.add(idx2);
					// Add to list
					gtList.add(pair);
					// Avg weights
					gtWeightList.add(weight);
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				System.exit(1);
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}

		/*
		 * Rescore the MeteorStats using different parameters and record results
		 */

		// Create configuration
		config = new MeteorConfiguration();
		config.setCharBased(charBased);
		ArrayList<Integer> none = new ArrayList<Integer>();
		config.setModules(none);
		weights = new ArrayList<Double>(initialWeights);

		int param = 0;
		rerank(param);
	}

	private static void rerank(int param) {
		// Rescore if all weights specified
		if (param == step.size()) {

			ArrayList<Double> p = new ArrayList<Double>();
			p.add(weights.get(0));
			p.add(weights.get(1));
			p.add(weights.get(2));
			p.add(weights.get(3));

			ArrayList<Double> w = new ArrayList<Double>();
			w.add(weights.get(4));
			w.add(weights.get(5));
			w.add(weights.get(6));
			w.add(weights.get(7));

			config.setParameters(p);
			config.setModuleWeights(w);

			MeteorScorer scorer = new MeteorScorer(config);

			ArrayList<Double> meteorScore = new ArrayList<Double>();

			for (int seg = 0; seg < statsList.size(); seg++) {
				MeteorStats stats = statsList.get(seg);
				scorer.computeMetrics(stats);
				meteorScore.add(stats.score);
			}

			double consist = kendall(meteorScore);

			out.print(consist);
			for (Double n : weights)
				out.print(" " + df.format(n));
			out.println();
			return;
		}

		for (double n = initialWeights.get(param); n <= finalWeights.get(param); n += step
				.get(param)) {
			weights.set(param, n);
			rerank(param + 1);
		}
	}

	private static double kendall(ArrayList<Double> meteorScore) {

		double correct = 0;
		double total = 0;
		for (int i = 0; i < gtList.size(); i++) {
			ArrayList<Integer> pair = gtList.get(i);
			// double weight = gtWeightList.get(i);
			double diff = meteorScore.get(pair.get(0))
					- meteorScore.get(pair.get(1));
			if (diff > eps)
				correct += 1; // weight;
			total += 1; // weight;
		}
		// Also known as rankconsist - (1 - rankconsist)
		return (correct - (total - correct)) / total;
	}

	private static ArrayList<Double> makePaddedList(String values) {
		ArrayList<Double> list = new ArrayList<Double>();
		StringTokenizer tok = new StringTokenizer(values);
		while (tok.hasMoreTokens())
			list.add(Double.parseDouble(tok.nextToken()));
		while (list.size() < INITIAL.length)
			list.add(0.0);
		return list;
	}

	private static String[] getMArgs(String testFile, String refFile,
			String paraFile, boolean charBased, boolean noNorm, boolean multi) {

		int langID = Constants.getLanguageID(Constants
				.normLanguageName(language));
		ArrayList<Integer> mods = Constants.getModules(langID,
				Constants.getDefaultTask(langID));
		String modString = "";
		String weightString = "";
		for (int mod : mods) {
			modString += Constants.getModuleName(mod) + " ";
			if (weightString.equals(""))
				weightString = "1.0 ";
			else
				weightString += "0.5 ";
		}
		modString = modString.trim();
		weightString = weightString.trim();
		String[] mArgs = { testFile, refFile, "-sgml", "-ssOut", "-l",
				language, "-m", modString, "-a", paraFile, "-w", weightString,
				"-p", "0.5 0.5 0.5 0.5" };
		if (charBased) {
			mArgs = Arrays.copyOf(mArgs, mArgs.length + 1);
			mArgs[mArgs.length - 1] = "-ch";
		}
		if (!noNorm) {
			mArgs = Arrays.copyOf(mArgs, mArgs.length + 1);
			mArgs[mArgs.length - 1] = "-norm";
		}
		// Overrides everything
		if (multi) {
			// fr~1-en.system.sgm -> en
			String basename = (new File(testFile)).getName();
			String lang = basename.substring(basename.indexOf("-") + 1,
					basename.indexOf("-") + 3);
			String[] multiArgs = {
					testFile,
					refFile,
					"-sgml",
					"-ssOut",
					"-l",
					lang,
					"-m",
					"exact paraphrase",
					"-a",
					Constants.getDefaultParaFileURL(
							Constants.getLanguageID(Constants
									.normLanguageName(lang))).getFile(), "-w",
					"1.0 0.5", "-p", "0.5 0.5 0.5 0.5", "-lower" };
			return multiArgs;
		}
		return mArgs;
	}
}
