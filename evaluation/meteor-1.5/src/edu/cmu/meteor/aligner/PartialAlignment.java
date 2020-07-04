/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.aligner;

import java.util.Arrays;

public class PartialAlignment {

	// Alignment
	public Match[] matches;

	// Search path information
	public int matchCount;
	public int matches1;
	public int matches2;
	public int allMatches1;
	public int allMatches2;
	public int chunks;
	public int idx;
	public int lastMatchEnd;
	public int distance;

	// Words already used
	public boolean[] line1UsedWords;
	public boolean[] line2UsedWords;

	// For creating start state
	public PartialAlignment(Match[] matches, boolean[] line1UsedWords,
			boolean[] line2UsedWords) {
		this.matches = Arrays.copyOf(matches, matches.length);
		this.matchCount = 0;
		this.matches1 = 0;
		this.matches2 = 0;
		this.allMatches1 = 0;
		this.allMatches2 = 0;
		this.chunks = 0;
		this.idx = 0;
		this.lastMatchEnd = -1;
		this.distance = 0;
		this.line1UsedWords = Arrays.copyOf(line1UsedWords,
				line1UsedWords.length);
		this.line2UsedWords = Arrays.copyOf(line2UsedWords,
				line2UsedWords.length);
	}

	// For creating a new path to be extended
	public PartialAlignment(PartialAlignment path) {
		this.matches = Arrays.copyOf(path.matches, path.matches.length);
		this.matchCount = path.matchCount;
		this.matches1 = path.matches1;
		this.matches2 = path.matches2;
		this.allMatches1 = path.allMatches1;
		this.allMatches2 = path.allMatches2;
		this.chunks = path.chunks;
		this.idx = path.idx;
		this.lastMatchEnd = path.lastMatchEnd;
		this.distance = path.distance;
		this.line1UsedWords = Arrays.copyOf(path.line1UsedWords,
				path.line1UsedWords.length);
		this.line2UsedWords = Arrays.copyOf(path.line2UsedWords,
				path.line2UsedWords.length);
	}

	// Check if match words are used
	public boolean isUsed(Match m) {
		for (int i = 0; i < m.length; i++)
			if (line2UsedWords[m.start + i])
				return true;
		for (int i = 0; i < m.matchLength; i++)
			if (line1UsedWords[m.matchStart + i])
				return true;
		return false;
	}

	// Set match words used/unused
	public void setUsed(Match m, boolean b) {
		for (int i = 0; i < m.length; i++)
			line2UsedWords[m.start + i] = b;
		for (int i = 0; i < m.matchLength; i++)
			line1UsedWords[m.matchStart + i] = b;
	}
}
