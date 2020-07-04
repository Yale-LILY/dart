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
import java.util.Arrays;

public class Stage {

	// Word keys
	public int[] words1;
	public int[] words2;

	// List of matches for each start index
	public ArrayList<ArrayList<Match>> matches;

	// Counts of matches covering each index
	public int[] line1Coverage;
	public int[] line2Coverage;

	Stage(ArrayList<String> wordStrings1, ArrayList<String> wordStrings2) {
		words1 = wordsToKeys(wordStrings1);
		words2 = wordsToKeys(wordStrings2);

		matches = new ArrayList<ArrayList<Match>>();
		for (int i = 0; i < words2.length; i++)
			matches.add(new ArrayList<Match>());

		line1Coverage = new int[words1.length];
		Arrays.fill(line1Coverage, 0);

		line2Coverage = new int[words2.length];
		Arrays.fill(line2Coverage, 0);
	}

	private int[] wordsToKeys(ArrayList<String> words) {
		int[] keys = new int[words.size()];
		for (int i = 0; i < words.size(); i++)
			// Chance of collision statistically insignificant,
			// no need for dictionary
			keys[i] = words.get(i).hashCode();
		return keys;
	}
}
