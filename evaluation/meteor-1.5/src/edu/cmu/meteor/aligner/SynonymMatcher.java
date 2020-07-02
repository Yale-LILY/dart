/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.aligner;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class SynonymMatcher {

	public static void match(int stage, Alignment a, Stage s,
			SynonymDictionary synonyms) {

		// Map words to sets of synonym set numbers

		Hashtable<Integer, HashSet<Integer>> string1Syn = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> string2Syn = new Hashtable<Integer, HashSet<Integer>>();

		// Line 1
		for (int i = 0; i < a.words1.size(); i++) {
			HashSet<Integer> set = new HashSet<Integer>(synonyms
					.getSynSets(a.words1.get(i)));
			set.addAll(synonyms.getStemSynSets(a.words1.get(i)));
			string1Syn.put(i, set);
		}

		// Line 2
		for (int i = 0; i < a.words2.size(); i++) {
			HashSet<Integer> set = new HashSet<Integer>(synonyms
					.getSynSets(a.words2.get(i)));
			set.addAll(synonyms.getStemSynSets(a.words2.get(i)));
			string2Syn.put(i, set);
		}

		for (int j = 0; j < a.words2.size(); j++) {

			for (int i = 0; i < a.words1.size(); i++) {

				Iterator<Integer> sets1 = string1Syn.get(i).iterator();
				HashSet<Integer> sets2 = string2Syn.get(j);

				boolean syn = false;
				double weight = 0;
				while (sets1.hasNext()) {
					if (sets2.contains(sets1.next())) {
						syn = true;
						weight = 1;
						break;
					}
				}

				// Match if DIFFERENT words with SAME synset
				if (syn && s.words1[i] != s.words2[j]) {

					Match m = new Match();
					m.module = stage;
					m.prob = weight;
					m.start = j;
					m.length = 1;
					m.matchStart = i;
					m.matchLength = 1;

					// Add this match to the list of matches and mark coverage
					s.matches.get(j).add(m);
					s.line1Coverage[i]++;
					s.line2Coverage[j]++;
				}
			}
		}
	}
}