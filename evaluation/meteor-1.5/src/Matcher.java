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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.cmu.meteor.aligner.Aligner;
import edu.cmu.meteor.aligner.Alignment;
import edu.cmu.meteor.aligner.PartialAlignment;
import edu.cmu.meteor.util.Constants;

public class Matcher {
	public static void main(String[] args) throws Exception {

		// Usage
		if (args.length < 2) {
			System.out.println("Meteor Aligner version " + Constants.VERSION);
			System.out.println("Usage: java -Xmx2G -cp meteor-*.jar Matcher "
					+ "<test> <reference> [options]");
			System.out.println();
			System.out.println("Options:");
			System.out
					.println("-l language                     One of: en da de es fi fr hu it nl no pt ro ru se tr");
			System.out
					.println("-m 'module1 module2 ...'        Specify modules (overrides default)");
			System.out
					.println("                                  One of: exact stem synonym paraphrase");
			System.out
					.println("-t type                         Alignment type (coverage vs accuracy)");
			System.out
					.println("                                  One of: maxcov maxacc");
			System.out
					.println("-x beamSize                     Keep speed reasonable");
			System.out
					.println("-d synonymDirectory             (if not default)");
			System.out
					.println("-a paraphraseFile               (if not default)");
			System.out
					.println("-stdio                          Read lines from stdin");
			System.out
					.println("                                  sentence 1 ||| sentence 2");
			System.out
					.println("                                  use \"-\" for test and reference (Matcher - - -stdio)");
			System.out.println();
			System.out.println("See README file for examples");
			return;
		}

		// Files
		String test = args[0];
		String ref = args[1];
		boolean stdio = false;

		Properties props = new Properties();

		// Input args
		int curArg = 2;
		while (curArg < args.length) {
			if (args[curArg].equals("-l")) {
				props.setProperty("language", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-x")) {
				props.setProperty("beamSize", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-d")) {
				props.setProperty("synDir", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-a")) {
				props.setProperty("paraFile", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-m")) {
				props.setProperty("modules", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-t")) {
				props.setProperty("type", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-stdio")) {
				stdio = true;
				curArg += 1;
			} else {
				System.err.println("Unknown option \"" + args[curArg] + "\"");
				System.exit(1);
			}
		}

		// Language
		String language = props.getProperty("language");
		if (language == null)
			language = "english";
		language = Constants.normLanguageName(language);

		// Synonym Location
		String synDir = props.getProperty("synDir");
		URL synURL;
		if (synDir == null)
			synURL = Constants.DEFAULT_SYN_DIR_URL;
		else
			synURL = (new File(synDir)).toURI().toURL();

		// Paraphrase Location
		String paraFile = props.getProperty("paraFile");
		URL paraURL;
		if (paraFile == null)
			paraURL = Constants.getDefaultParaFileURL(Constants
					.getLanguageID(language));
		else
			paraURL = (new File(paraFile)).toURI().toURL();

		// Max Computations
		String beam = props.getProperty("beamSize");
		int beamSize = 0;
		if (beam == null)
			beamSize = Constants.DEFAULT_BEAM_SIZE;
		else
			beamSize = Integer.parseInt(beam);

		// Modules
		String modNames = props.getProperty("modules");
		if (modNames == null)
			modNames = "exact stem synonym paraphrase";
		ArrayList<Integer> modules = new ArrayList<Integer>();
		StringTokenizer mods = new StringTokenizer(modNames);
		while (mods.hasMoreTokens()) {
			int module = Constants.getModuleID(mods.nextToken());
			modules.add(module);
		}

		// Alignment Type
		String type = props.getProperty("type");
		if (type == null)
			type = "maxcov";
		Comparator<PartialAlignment> partialComparator;
		ArrayList<Double> moduleWeights = new ArrayList<Double>();
		if (type.equals("maxcov")) {
			partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
			for (int module : modules) {
				if (module == Constants.MODULE_EXACT)
					moduleWeights.add(1.0);
				else if (module == Constants.MODULE_STEM)
					moduleWeights.add(0.5);
				else if (module == Constants.MODULE_SYNONYM)
					moduleWeights.add(0.5);
				else
					moduleWeights.add(0.5);
			}
		} // maxacc
		else {
			partialComparator = Constants.PARTIAL_COMPARE_TOTAL_ALL;
			for (int module : modules) {
				if (module == Constants.MODULE_EXACT)
					moduleWeights.add(1.0);
				else if (module == Constants.MODULE_STEM)
					moduleWeights.add(1.0);
				else if (module == Constants.MODULE_SYNONYM)
					moduleWeights.add(1.0);
				else
					moduleWeights.add(0.0);
			}
		}

		// Construct aligner
		Aligner aligner = new Aligner(language, modules, moduleWeights,
				beamSize, Constants.getDefaultWordFileURL(Constants
						.getLanguageID(language)), synURL, paraURL,
				partialComparator);

		// Stdio: one input per line from stdin
		if (stdio) {
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			while ((line = in.readLine()) != null) {
				int split = line.indexOf(" ||| ");
				if (split == -1) {
					System.err
							.println("Format error, use: sentence 1 ||| sentence 2");
					continue;
				}
				Alignment a = aligner.align(line.substring(0, split),
						line.substring(split + 5));
				System.out.println(a.toString());
			}
		} else {
			// Open files
			BufferedReader inTest = null;
			BufferedReader inRef = null;
			try {
				inTest = new BufferedReader(new InputStreamReader(
						new FileInputStream(test), "UTF-8"));
				inRef = new BufferedReader(new InputStreamReader(
						new FileInputStream(ref), "UTF-8"));
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}

			// Read lines
			String lineTest;
			String lineRef;
			int line = 0;
			while ((lineTest = inTest.readLine()) != null) {
				lineRef = inRef.readLine();
				if (lineRef == null) {
					System.err.println("Error: files not of same length.");
					System.exit(1);
				}
				line++;
				try {
					// Align
					Alignment a = aligner.align(lineTest, lineRef);
					// Output results
					System.out.println(a.toString("Alignment " + line));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
