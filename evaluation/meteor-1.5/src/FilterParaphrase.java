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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.cmu.meteor.util.Constants;
import edu.cmu.meteor.util.Normalizer;

public class FilterParaphrase {
	public static void main(String[] args) throws IOException,
			FileNotFoundException {

		if (args.length < 3) {
			System.out.println("Filter Paraphrase Table:");
			System.out.println();
			System.out
					.println("Filter paraphrase file to one or more references.  Make sure to select the\n"
							+ "correct language paraphrase file (normally in data dir) and reference file.\n"
							+ "Specify the location of the filtered file to the Meteor jar with the -a option.");
			System.out.println();
			System.out.println("Usage: java -cp meteor-*.jar FilterParaphrase"
					+ " <paraphrase.gz> <filtered.gz> <ref1> [ref2 ...]");
			System.out.println();
			System.out.println("See README file for examples");
			System.exit(1);
		}

		String paraFile = args[0];
		String filteredFile = args[1];

		Hashtable<Integer, Hashtable> phrases = new Hashtable<Integer, Hashtable>();

		System.out.println("Reading reference files...");
		for (int i = 2; i < args.length; i++) {
			System.out.println(args[i]);
			String refFile = args[i];

			BufferedReader in = new BufferedReader(new FileReader(refFile));
			String line;
			while ((line = in.readLine()) != null) {
				String normLine = Normalizer.normalizeLine(line,
						Constants.LANG_EN, false).toLowerCase();
				StringTokenizer tok = new StringTokenizer(normLine);
				ArrayList<Integer> words = new ArrayList<Integer>();
				while (tok.hasMoreTokens())
					words.add(tok.nextToken().hashCode());
				for (int j = 0; j < words.size(); j++)
					add(words, phrases, j);
			}
			in.close();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream((new File(paraFile)).toURI().toURL()
						.openStream()), "UTF-8"));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(filteredFile)),
				"UTF-8"));
		int count = 0;
		int used = 0;
		System.out.println("Filtering paraphrases...");
		String line;
		while ((line = in.readLine()) != null) {
			String line2 = in.readLine();
			String line3 = in.readLine();
			boolean print = false;
			StringTokenizer tok = new StringTokenizer(line2);
			ArrayList<Integer> words = new ArrayList<Integer>();
			while (tok.hasMoreTokens())
				words.add(tok.nextToken().hashCode());
			if (check(words, phrases))
				print = true;
			if (!print) {
				tok = new StringTokenizer(line3);
				words = new ArrayList<Integer>();
				while (tok.hasMoreTokens())
					words.add(tok.nextToken().hashCode());
				if (check(words, phrases))
					print = true;
			}
			if (print) {
				used++;
				out.println(line);
				out.println(line2);
				out.println(line3);
			}
			count++;
			if (count % 1000000 == 0)
				System.out.println(count + " (" + used + ")");
		}
		in.close();
		out.close();
	}

	private static void add(ArrayList<Integer> words,
			Hashtable<Integer, Hashtable> phrases, int index) {
		for (int i = index; i < words.size(); i++) {
			if (!phrases.containsKey(words.get(i)))
				phrases.put(words.get(i), new Hashtable<Integer, Hashtable>());
			phrases = phrases.get(words.get(i));
		}
	}

	private static boolean check(ArrayList<Integer> words,
			Hashtable<Integer, Hashtable> phrases) {
		for (int i = 0; i < words.size(); i++) {
			if (!phrases.containsKey(words.get(i)))
				return false;
			phrases = phrases.get(words.get(i));
		}
		return true;
	}
}
