package edu.cmu.parex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ParaphraseExtractor {

	public static final double MIN_TRANS_PROB = 0.001;

	public static final double MIN_REL_FREQ = 0.001;

	public static final double MIN_FINAL_PROB = 0.01;

	public static final String SYMBOLS = "~`!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

	// Check if phrase is clean of symbols
	private static boolean isClean(String s, HashSet<Character> symbols) {
		for (int i = 0; i < s.length(); i++)
			if (symbols.contains(s.charAt(i)))
				return false;
		return true;
	}

	// Check if phrase contains at least one uncommon word
	private static boolean isUsable(int[] words, HashSet<Integer> commons) {
		for (int word : words)
			if (!commons.contains(word))
				return true;
		return false;
	}

	// Check if 2 word (int) arrays are equal
	private static boolean eqWords(int[] words1, int[] words2) {
		if (words1.length != words2.length)
			return false;
		for (int i = 0; i < words1.length; i++)
			if (words1[i] != words2[i])
				return false;
		return true;
	}

	// Paraphrasing via pivot approach
	public static void extractParaphrases(String targetCorpusFile,
			String phrasetableFile, String fCommonFile, String eCommonFile,
			String outFile, double minTransProb, String symbolString)
			throws IOException {

		// Table stores (pivot, reference, p(piv|ref))
		// used to trace paraphrases back to ref
		PhraseTable pt = new PhraseTable();

		Hashtable<Integer, Hashtable> corpus = new Hashtable<Integer, Hashtable>();
		HashSet<Character> symbols = new HashSet<Character>();
		for (int i = 0; i < symbolString.length(); i++)
			symbols.add(symbolString.charAt(i));

		// Load corpus
		System.err.println("Loading corpus");
		BufferedReader in = new BufferedReader(new FileReader(targetCorpusFile));
		String line;
		while ((line = in.readLine()) != null) {
			int[] words = pt.mapPhrase(line);
			// For each start index
			for (int i = 0; i < words.length; i++) {
				// Load the (n-i)-gram
				Hashtable<Integer, Hashtable> table = corpus;
				for (int j = i; j < words.length; j++) {
					if (!table.containsKey(words[j]))
						table.put(words[j], new Hashtable<Integer, Hashtable>());
					table = table.get(words[j]);
				}
			}
		}

		HashSet<Integer> fCommons = new HashSet<Integer>();
		HashSet<Integer> eCommons = new HashSet<Integer>();

		// Load common words
		System.err.println("Loading common words (foreign)");
		in = new BufferedReader(new FileReader(fCommonFile));
		while ((line = in.readLine()) != null) {
			fCommons.add(pt.mapWord(line));
		}
		System.err.println("Loading common words (english)");
		in = new BufferedReader(new FileReader(eCommonFile));
		while ((line = in.readLine()) != null) {
			eCommons.add(pt.mapWord(line));
		}

		URL ptFile = (new File(phrasetableFile)).toURI().toURL();
		in = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				ptFile.openStream())));

		int lineCount = 0;
		int phraseCount = 0;
		System.err.println("Loading phrases");
		while ((line = in.readLine()) != null) {

			try {
				lineCount++;
				if (lineCount % 10000000 == 0)
					System.err.println(lineCount + " (" + phraseCount + ")");

				String[] part = line.split("\\|\\|\\|");

				// Foreign phrase 1
				String f1 = part[0].trim();
				// English phrase 1
				String e1 = part[1].trim();
				// Probabilities
				StringTokenizer pTok = new StringTokenizer(part[2]);
				double pf1Ge1 = Double.parseDouble(pTok.nextToken());
				pTok.nextToken();
				double pe1Gf1 = Double.parseDouble(pTok.nextToken());

				// Original phrase and pivot
				String phrase1 = e1;
				String pivot1 = f1;
				double prob = pf1Ge1;
				HashSet<Integer> p1commons = eCommons;
				HashSet<Integer> piv1commons = fCommons;

				// Skip if this will only make low scoring paraphrases
				if (prob < minTransProb)
					continue;

				// Vacuum phrases with symbols
				if (!isClean(phrase1, symbols) || !isClean(pivot1, symbols)) {
					continue;
				}

				int[] p1 = pt.mapPhrase(phrase1);
				int[] piv1 = pt.mapPhrase(pivot1);

				// Vacuum phrases with only common words
				if (!isUsable(p1, p1commons) || !isUsable(piv1, piv1commons)) {
					continue;
				}

				// Check if phrase1 (ref) in corpus
				boolean inCorpus = true;
				Hashtable<Integer, Hashtable> table = corpus;
				for (int word : p1) {
					if (!table.containsKey(word)) {
						inCorpus = false;
						break;
					}
					table = table.get(word);
				}
				// If not, skip entry
				if (!inCorpus)
					continue;

				// Otherwise store phrase entry
				pt.addPhrasePair(piv1, p1, prob);
				phraseCount++;
			} catch (Exception ex) {
				System.err.println("Skipping problematic line: " + line);
			}
		}
		in.close();

		// For writing paraphrases
		PrintWriter eOut = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(new File(outFile))));

		// Second read, look for paraphrases
		in = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				ptFile.openStream())));
		lineCount = 0;
		phraseCount = 0;
		System.err.println("Finding paraphrases");
		while ((line = in.readLine()) != null) {

			try {
				lineCount++;
				if (lineCount % 10000000 == 0)
					System.err.println(lineCount + " (" + phraseCount + ")");

				String[] part = line.split("\\|\\|\\|");

				// Foreign2
				String f2 = part[0].trim();
				// English2
				String e2 = part[1].trim();
				// Probabilities
				StringTokenizer pTok = new StringTokenizer(part[2]);
				double pf2Ge2 = Double.parseDouble(pTok.nextToken());
				pTok.nextToken();
				double pe2Gf2 = Double.parseDouble(pTok.nextToken());

				// Paraphrase and pivot
				String phrase2 = e2;
				String pivot2 = f2;
				double prob = pe2Gf2;
				HashSet<Integer> p2commons = eCommons;
				HashSet<Integer> piv2commons = fCommons;

				// Skip if this will only make low scoring paraphrases
				if (prob < minTransProb)
					continue;

				// Vacuum phrases with symbols
				if (!isClean(phrase2, symbols) || !isClean(pivot2, symbols))
					continue;

				int[] p2 = pt.mapPhrase(phrase2);
				int[] piv2 = pt.mapPhrase(pivot2);

				// Vacuum phrases with only common words
				if (!isUsable(p2, p2commons) || !isUsable(piv2, piv2commons)) {
					continue;
				}

				// For phrases p1 with piv1 == piv2
				for (PhraseTable.Phrase p1 : pt.getPhrases(piv2)) {
					// p1 != p2
					if (eqWords(p1.words, p2))
						continue;
					// p = p(phrase1|piv1) * p(phrase2|piv2)
					double parProb = p1.prob * prob;
					if (parProb < minTransProb)
						continue;
					// reference ||| paraphrase ||| pivot ||| prob
					eOut.println(p1.phrase + " ||| " + phrase2 + " ||| "
							+ pivot2 + " ||| " + parProb);
					phraseCount++;
				}
			} catch (Exception ex) {
				System.err.println("Skipping problematic line: " + line);
			}

		}
		in.close();
		eOut.close();
	}

	public static void findCommonWords(String corpus, String outFile,
			double minRF) throws IOException {

		// Count words in corpus
		Hashtable<String, Integer> wc = new Hashtable<String, Integer>();
		int total = 0;
		BufferedReader in = new BufferedReader(new FileReader(corpus));
		String line;
		while ((line = in.readLine()) != null) {
			StringTokenizer tok = new StringTokenizer(line);
			while (tok.hasMoreTokens()) {
				String word = tok.nextToken();
				Integer i = wc.get(word);
				if (i == null)
					i = 0;
				wc.put(word, i + 1);
				total++;
			}
		}
		in.close();

		// Write out common words
		PrintWriter out = new PrintWriter(outFile);
		Enumeration<String> e = wc.keys();
		while (e.hasMoreElements()) {
			String word = e.nextElement();
			double rf = ((double) wc.get(word)) / total;
			if (rf > minRF)
				out.println(word);
		}
		out.close();
	}

	public static void groupParaphrases(String paraphraseFile,
			String groupedFile) throws IOException {

		URL rawFile = (new File(paraphraseFile)).toURI().toURL();
		File sortFile = new File(groupedFile);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(rawFile.openStream())));

		// For dictionary
		final PhraseTable pt = new PhraseTable();

		ArrayList<Paraphrase> paraphrases = new ArrayList<Paraphrase>();

		String line;
		int i = 0;
		while ((line = in.readLine()) != null) {
			i++;
			if (i % 1000000 == 0)
				System.err.println(i);
			String[] entry = line.split("\\|\\|\\|");
			String ref = entry[0].trim();
			String par = entry[1].trim();
			double prob = Double.parseDouble(entry[3].trim());
			paraphrases.add(new Paraphrase(pt.mapPhrase(ref),
					pt.mapPhrase(par), prob));
		}
		in.close();

		// Sort
		Collections.sort(paraphrases, new Comparator<Paraphrase>() {
			public int compare(Paraphrase p1, Paraphrase p2) {
				// First unmap and compare refs
				int diff = pt.unmapPhrase(p1.ref).compareTo(
						pt.unmapPhrase(p2.ref));
				// If not equal, return diff
				if (diff != 0)
					return diff;
				// Else compare paraphrases
				return pt.unmapPhrase(p1.par).compareTo(pt.unmapPhrase(p2.par));
			}
		});

		PrintWriter out = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(sortFile)));
		for (Paraphrase p : paraphrases)
			out.println(pt.unmapPhrase(p.ref) + " ||| " + pt.unmapPhrase(p.par)
					+ " ||| " + p.prob);
		out.close();
	}

	// Merge and vacuum
	public static void combineParaphrases(String groupedFile,
			String finalParaphraseFile, double minProb) throws IOException {

		URL sortFile = (new File(groupedFile)).toURI().toURL();
		File finalFile = new File(finalParaphraseFile);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(sortFile.openStream())));
		PrintWriter out = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(finalFile)));

		String curRef = "";
		String curPar = "";
		double curProb = 0;
		String line;
		while ((line = in.readLine()) != null) {
			String[] entry = line.split("\\|\\|\\|");
			String ref = entry[0].trim();
			String par = entry[1].trim();
			double prob = Double.parseDouble(entry[2].trim());
			if (!ref.equals(curRef) || !par.equals(curPar)) {
				// Also vacuum with minProb, substring
				if (!curRef.equals("") && curProb >= minProb
						&& !subphrase(curRef, curPar))
					out.println(curRef + " ||| " + curPar + " ||| " + curProb);
				curRef = ref;
				curPar = par;
				curProb = 0;
			}
			curProb += prob;
		}
		// Also vacuum with minProb, substring
		if (!curRef.equals("") && curProb >= minProb
				&& !subphrase(curRef, curPar))
			out.println(curRef + " ||| " + curPar + " ||| " + curProb);

		in.close();
		out.close();
	}

	// True if p1 is a substring of p2
	public static boolean subphrase(String p1, String p2) {
		String sp1 = " " + p1 + " ";
		String sp2 = " " + p2 + " ";
		return (sp1.contains(sp2) || sp2.contains(sp1));
	}
}
