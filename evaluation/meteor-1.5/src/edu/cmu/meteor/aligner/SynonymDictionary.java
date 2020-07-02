/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.aligner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class SynonymDictionary {

	// Exceptions
	private Hashtable<String, ArrayList<String>> wordToBases;

	// Synsets
	private Hashtable<String, HashSet<Integer>> wordToSynsets;

	// Relations
	private Hashtable<Integer, HashSet<Integer>> setToRelations;

	public SynonymDictionary(URL excFileURL, URL synFileURL, URL relFileURL)
			throws IOException {
		wordToBases = new Hashtable<String, ArrayList<String>>();
		wordToSynsets = new Hashtable<String, HashSet<Integer>>();
		setToRelations = new Hashtable<Integer, HashSet<Integer>>();

		// Exception file
		BufferedReader inExc = new BufferedReader(new InputStreamReader(
				excFileURL.openStream(), "UTF-8"));
		String base;
		String line;
		while ((base = inExc.readLine()) != null) {
			line = inExc.readLine();
			StringTokenizer tok = new StringTokenizer(line);
			while (tok.hasMoreTokens()) {
				String form = tok.nextToken();
				ArrayList<String> bases = wordToBases.get(form);
				if (bases == null) {
					bases = new ArrayList<String>();
					wordToBases.put(form, bases);
				}
				bases.add(base);
			}
		}
		inExc.close();

		// Synset file
		BufferedReader inSyn = new BufferedReader(new InputStreamReader(
				synFileURL.openStream()));
		String word;
		while ((word = inSyn.readLine()) != null) {
			HashSet<Integer> set = new HashSet<Integer>();
			line = inSyn.readLine();
			StringTokenizer tok = new StringTokenizer(line);
			while (tok.hasMoreTokens())
				set.add(Integer.parseInt(tok.nextToken()));
			wordToSynsets.put(word, set);
		}
		inSyn.close();

		// Relation file

		// No useful method for incorporating this data has yet been
		// implemented, so there is no need to load the data file
		boolean LOAD_DATA = false;

		if (LOAD_DATA) {
			BufferedReader inRel = new BufferedReader(new InputStreamReader(
					relFileURL.openStream()));
			String set;
			while ((set = inRel.readLine()) != null) {
				HashSet<Integer> relations = new HashSet<Integer>();
				line = inRel.readLine();
				StringTokenizer tok = new StringTokenizer(line);
				while (tok.hasMoreTokens())
					relations.add(Integer.parseInt(tok.nextToken()));
				setToRelations.put(Integer.parseInt(set), relations);
			}
			inRel.close();
		}

	}

	public HashSet<Integer> getSynSets(String word) {
		HashSet<Integer> set = wordToSynsets.get(word);
		if (set != null)
			return set;
		return new HashSet<Integer>();
	}

	public HashSet<Integer> getStemSynSets(String word) {
		ArrayList<String> bases = wordToBases.get(word);
		if (bases != null) {
			HashSet<Integer> set = new HashSet<Integer>();
			for (String base : bases)
				set.addAll(getSynSets(base));
			return set;
		}
		return getSynSets(morph(word));
	}

	public HashSet<Integer> getRelations(int set) {
		HashSet<Integer> relations = setToRelations.get(set);
		if (relations != null)
			return relations;
		return new HashSet<Integer>();
	}

	/*
	 * This information and the morphology algorithm are taken from the WordNet
	 * 3 release. See the WordNet license replicated at the end of this file.
	 */

	private static final int OFFSET = 0;
	private static final int CNT = 20;

	private static final String[] sufx = {
	/* Noun suffixes */
	"s", "ses", "xes", "zes", "ches", "shes", "men", "ies",
	/* Verb suffixes */
	"s", "ies", "es", "es", "ed", "ed", "ing", "ing",
	/* Adjective suffixes */
	"er", "est", "er", "est" };

	private static final String[] addr = {
	/* Noun endings */
	"", "s", "x", "z", "ch", "sh", "man", "y",
	/* Verb endings */
	"", "y", "e", "", "e", "", "e", "",
	/* Adjective endings */
	"", "", "e", "e" };

	private String morph(String word) {
		String tmp = "";
		String end = "";
		String retval = "";

		if (word.endsWith("ful")) {
			tmp = word.substring(0, word.lastIndexOf('f'));
			end = "ful";
		}

		if (word.endsWith("ss") || word.length() <= 2)
			return word;

		tmp = word;

		for (int i = 0; i < CNT; i++) {
			int ender = i + OFFSET;
			if (tmp.endsWith(sufx[ender]))
				retval = word
						.substring(0, word.length() - sufx[ender].length())
						+ addr[ender];
			else
				retval = tmp;
			if (retval != tmp && wordToSynsets.containsKey(retval)) {
				retval += end;
				return retval;
			}
		}

		return "";
	}
}

/*
 * WordNet Release 3.0
 * 
 * This software and database is being provided to you, the LICENSEE, by
 * Princeton University under the following license. By obtaining, using and/or
 * copying this software and database, you agree that you have read, understood,
 * and will comply with these terms and conditions.:
 * 
 * Permission to use, copy, modify and distribute this software and database and
 * its documentation for any purpose and without fee or royalty is hereby
 * granted, provided that you agree to comply with the following copyright
 * notice and statements, including the disclaimer, and that the same appear on
 * ALL copies of the software, database and documentation, including
 * modifications that you make for internal use or for distribution.
 * 
 * WordNet 3.0 Copyright 2006 by Princeton University. All rights reserved.
 * 
 * THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" AND PRINCETON UNIVERSITY MAKES
 * NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED. BY WAY OF EXAMPLE, BUT
 * NOT LIMITATION, PRINCETON UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES
 * OF MERCHANT- ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF
 * THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD
 * PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 * 
 * The name of Princeton University or Princeton may not be used in advertising
 * or publicity pertaining to distribution of the software and/or database.
 * Title to copyright in this software, database and any associated
 * documentation shall at all times remain with Princeton University and
 * LICENSEE agrees to preserve same.
 */