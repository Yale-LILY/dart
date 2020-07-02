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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

public class ParaphraseTransducer {

	private State reference;
	private ArrayList<int[]> paraphrase;
	private int nextIdx;

	private Hashtable<String, Integer> vocab;
	private int nextWord;

	// Any state in the transducer
	private class State {
		private Hashtable<Integer, State> trans;
		private int[] emit;

		public State() {
			// Use as little memory as possible
			trans = new Hashtable<Integer, State>(0);
			emit = new int[0];
		}

		// Add path through states starting with word words[startIdx] using
		// remaining words and emit index emitIdx
		public void addPath(int[] words, int startIdx, int emitIdx) {
			if (startIdx == words.length) {
				// Spend cpu time instead of memory
				emit = Arrays.copyOf(emit, emit.length + 1);
				emit[emit.length - 1] = emitIdx;
				return;
			}
			int word = words[startIdx];
			State p = trans.get(word);
			if (p == null) {
				p = new State();
				trans.put(word, p);
			}
			p.addPath(words, startIdx + 1, emitIdx);
		}

		// Returns an array of length 2*paths in the form:
		// [phrase1length, phrase1index, p2l, p2i, ...]
		// for words starting at startIdx
		public int[] getPaths(int[] words, int startIdx) {
			ArrayList<Integer> paths = new ArrayList<Integer>();
			State p = this;
			for (int i = 0; i < words.length - startIdx; i++) {
				p = p.trans.get(words[startIdx + i]);
				if (p == null) {
					break;
				}
				// If there are paths
				if (p.emit != null)
					for (int j : p.emit) {
						paths.add(i + 1);// length
						paths.add(j);// path index
					}
			}
			int[] pathArray = new int[paths.size()];
			for (int i = 0; i < paths.size(); i++)
				pathArray[i] = paths.get(i);
			return pathArray;
		}
	}

	private int map(String str) {
		Integer i = vocab.get(str);
		if (i == null) {
			i = new Integer(nextWord++);
			vocab.put(str, i);
		}
		return i;
	}

	public ParaphraseTransducer(URL paraphraseFileURL) {
		reference = new State();
		paraphrase = new ArrayList<int[]>();
		nextIdx = 0;

		vocab = new Hashtable<String, Integer>();
		nextWord = 0;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(paraphraseFileURL.openStream()),
					"UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				// These probabilities do not appear to correlate with actual
				// paraphrase probability
				// double p = Double.parseDouble(line);

				// Reference
				StringTokenizer tok = new StringTokenizer(in.readLine());
				int[] refWords = new int[tok.countTokens()];

				for (int idx = 0; tok.hasMoreTokens(); idx++)
					refWords[idx] = map(tok.nextToken());

				// Paraphrase
				tok = new StringTokenizer(in.readLine());
				int[] parWords = new int[tok.countTokens()];
				for (int idx = 0; tok.hasMoreTokens(); idx++)
					parWords[idx] = map(tok.nextToken());

				reference.addPath(refWords, 0, nextIdx++);
				paraphrase.add(parWords);
			}
		} catch (FileNotFoundException fe) {
			throw new RuntimeException("Error: file not found ("
					+ paraphraseFileURL + ")");
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public int translate(String str) {
		Integer i = vocab.get(str);
		if (i == null)
			return -1;
		return i;
	}

	public int[] translate(String[] str) {
		int[] is = new int[str.length];
		int i = 0;
		for (String s : str)
			is[i++] = translate(s);
		return is;
	}

	public int[] translate(ArrayList<String> str) {
		int[] is = new int[str.size()];
		int i = 0;
		for (String s : str)
			is[i++] = translate(s);
		return is;
	}

	public int[] getReferencePaths(int[] words, int startIdx) {
		return reference.getPaths(words, startIdx);
	}

	public int[] getParaphrase(int parIdx) {
		return paraphrase.get(parIdx);
	}
}