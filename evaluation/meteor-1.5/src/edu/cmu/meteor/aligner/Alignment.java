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
import java.util.HashSet;
import java.util.StringTokenizer;

public class Alignment {

	// Words in strings
	public ArrayList<String> words1;
	public ArrayList<String> words2;

	// Function word indices
	public HashSet<Integer> line1FunctionWords;
	public HashSet<Integer> line2FunctionWords;

	// matches[i] contains a match starting at index i in line2
	public Match[] matches;

	// Match totals
	public int line1Matches;
	public int line2Matches;

	// Per-module match totals (Content)
	public ArrayList<Integer> moduleContentMatches1;
	public ArrayList<Integer> moduleContentMatches2;

	// Per-module match totals (Function)
	public ArrayList<Integer> moduleFunctionMatches1;
	public ArrayList<Integer> moduleFunctionMatches2;

	// Chunks
	public int numChunks;
	public double avgChunkLength;

	// Lines as Strings
	public Alignment(String line1, String line2) {
		words1 = tokenize(line1);
		words2 = tokenize(line2);
		initData(words2.size());
	}

	// Lines as ArrayLists of tokenized lowercased Strings
	public Alignment(ArrayList<String> words1, ArrayList<String> words2) {
		this.words1 = new ArrayList<String>(words1);
		this.words2 = new ArrayList<String>(words2);
		initData(this.words2.size());
	}

	// Initialize values
	private void initData(int words2length) {
		line1FunctionWords = new HashSet<Integer>();
		line2FunctionWords = new HashSet<Integer>();

		matches = new Match[words2length];

		line1Matches = 0;
		line2Matches = 0;

		moduleContentMatches1 = new ArrayList<Integer>();
		moduleContentMatches2 = new ArrayList<Integer>();

		moduleFunctionMatches1 = new ArrayList<Integer>();
		moduleFunctionMatches2 = new ArrayList<Integer>();

		numChunks = 0;
		avgChunkLength = 0;
	}

	// Tokenize input line
	private ArrayList<String> tokenize(String line) {
		ArrayList<String> tokens = new ArrayList<String>();
		StringTokenizer tok = new StringTokenizer(line);
		while (tok.hasMoreTokens())
			tokens.add(tok.nextToken());
		return tokens;
	}

	public void printMatchedPhrases() {
		System.out.println(words1);
		System.out.println(words2);
		for (Match m : matches) {
			if (m != null) {
				String s = m.module + " : ";
				for (int i = m.start; i < m.start + m.length; i++)
					s += words2.get(i) + " ";
				s += "== ";
				for (int i = m.matchStart; i < m.matchStart + m.matchLength; i++)
					s += words1.get(i) + " ";
				System.out.println(s.trim());
			}
		}
	}

	public String toString() {
		return toString("Alignment");
	}

	public String toString(String header) {
		StringBuilder out = new StringBuilder();
		out.append(header + "\n");
		StringBuilder test = new StringBuilder();
		for (String s : words1)
			test.append(s + " ");
		out.append(test.toString().trim() + "\n");
		StringBuilder ref = new StringBuilder();
		for (String s : words2)
			ref.append(s + " ");
		out.append(ref.toString().trim() + "\n");
		out.append("Line2Start:Length\tLine1Start:Length\tModule\t\tScore\n");
		for (int j = 0; j < matches.length; j++) {
			Match m = matches[j];
			if (m != null) {
				// Second string word
				out.append(m.start + ":" + m.length + "\t\t\t");
				// First string word
				out.append(m.matchStart + ":" + m.matchLength + "\t\t\t");
				// Module stage
				out.append(m.module + "\t\t");
				// Score
				out.append(m.prob + "\n");
			}
		}
		return out.toString();
	}
}
