package edu.cmu.parex;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Unidirectional phrase table
 */

public class PhraseTable {
	public static final String UNK = "<UNK>";

	private Hashtable<String, Integer> dict;
	private Hashtable<Integer, String> revDict;
	private int nextWordInt;

	private Node base;

	private ArrayList<int[]> phrases;
	private ArrayList<Double> probs;
	private int nextPhraseIx;

	private class Node {
		public Hashtable<Integer, Node> next = new Hashtable<Integer, Node>();
		public int[] phraseIdx = new int[0];
	}

	public class Phrase {
		String phrase;
		int[] words;
		double prob;

		public Phrase(String phrase, int[] words, double prob) {
			this.phrase = phrase;
			this.words = words;
			this.prob = prob;
		}
	}

	public PhraseTable() {
		dict = new Hashtable<String, Integer>();
		revDict = new Hashtable<Integer, String>();
		nextWordInt = 0;

		base = new Node();

		phrases = new ArrayList<int[]>();
		probs = new ArrayList<Double>();
		nextPhraseIx = 0;
	}

	public int mapWord(String word) {
		Integer i = dict.get(word);
		if (i == null) {
			i = nextWordInt;
			dict.put(word, nextWordInt);
			revDict.put(nextWordInt, word);
			nextWordInt++;
		}
		return i;
	}

	public String unmapWord(int word) {
		String s = revDict.get(word);
		if (s == null)
			s = UNK;
		return s;
	}

	public int[] mapPhrase(String phrase) {
		StringTokenizer tok = new StringTokenizer(phrase);
		int[] result = new int[tok.countTokens()];
		for (int i = 0; tok.hasMoreTokens(); i++)
			result[i] = mapWord(tok.nextToken());
		return result;
	}

	public String unmapPhrase(int[] phrase) {
		StringBuilder result = new StringBuilder();
		for (int i : phrase) {
			result.append(unmapWord(i));
			result.append(" ");
		}
		return result.toString().trim();
	}

	public void addPhrasePair(String phrase1, String phrase2, double prob) {
		int[] p1 = mapPhrase(phrase1);
		int[] p2 = mapPhrase(phrase2);
		addPhrasePair(p1, p2, prob);
	}

	public void addPhrasePair(int[] phrase1, int[] phrase2, double prob) {
		// Add phrase1 path
		Node node = base;
		for (int word : phrase1) {
			Node next = node.next.get(word);
			if (next == null) {
				next = new Node();
				node.next.put(word, next);
			}
			node = next;
		}

		// Add phrase2, prob
		phrases.add(phrase2);
		probs.add(prob);

		// New phrase index including phrase
		int[] newPhraseIdx = new int[node.phraseIdx.length + 1];
		for (int i = 0; i < node.phraseIdx.length; i++)
			newPhraseIdx[i] = node.phraseIdx[i];
		newPhraseIdx[node.phraseIdx.length] = nextPhraseIx++;
		node.phraseIdx = newPhraseIdx;
	}

	public ArrayList<Phrase> getPhrases(String phrase) {
		int[] p = mapPhrase(phrase);
		return getPhrases(p);
	}

	public ArrayList<Phrase> getPhrases(int[] phrase) {
		// Follow phrase path
		Node node = base;
		for (int word : phrase) {
			Node next = node.next.get(word);
			// No phrase path
			if (next == null)
				return new ArrayList<Phrase>();
			node = next;
		}

		// Get phrases
		ArrayList<Phrase> result = new ArrayList<Phrase>();
		for (int i : node.phraseIdx)
			result.add(new Phrase(unmapPhrase(phrases.get(i)), phrases.get(i),
					probs.get(i)));
		return result;
	}
}
