/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.aligner;

import java.util.ArrayList;

public class StemMatcher {

	public static void match(int stage, Alignment a, Stage s, Stemmer stemmer) {

		// Get keys for word stems
		int[] stems1 = wordsToStemKeys(a.words1, stemmer);
		int[] stems2 = wordsToStemKeys(a.words2, stemmer);

		for (int j = 0; j < stems2.length; j++) {

			for (int i = 0; i < stems1.length; i++) {

				// Match for DIFFERENT words with SAME stems
				if (stems1[i] == stems2[j] && s.words1[i] != s.words2[j]) {

					Match m = new Match();
					m.module = stage;
					m.prob = 1;
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

	private static int[] wordsToStemKeys(ArrayList<String> words,
			Stemmer stemmer) {
		int[] keys = new int[words.size()];
		for (int i = 0; i < words.size(); i++) {
			// Stem the word before generating a key
			keys[i] = stemmer.stem(words.get(i)).hashCode();
		}
		return keys;
	}
}
