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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import edu.cmu.meteor.util.Constants;

public class Aligner {

	/* Configuration */

	private String language;

	private int moduleCount;
	private ArrayList<Integer> modules;
	private ArrayList<Double> moduleWeights;

	private int beamSize;

	private Stemmer stemmer;
	private SynonymDictionary synonyms;
	private ParaphraseTransducer paraphrase;
	private HashSet<String> functionWords;

	// Used for sorting partial alignments
	private Comparator<PartialAlignment> partialComparator;

	public Aligner(String language, ArrayList<Integer> modules) {
		this.beamSize = Constants.DEFAULT_BEAM_SIZE;
		this.partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
		setupModules(language, modules, Constants.DEFAULT_WORD_DIR_URL,
				Constants.DEFAULT_SYN_DIR_URL,
				Constants.getDefaultParaFileURL(Constants
						.getLanguageID(Constants.normLanguageName(language))));
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights) {
		this.beamSize = Constants.DEFAULT_BEAM_SIZE;
		this.partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
		setupModules(language, modules, Constants.DEFAULT_WORD_DIR_URL,
				Constants.DEFAULT_SYN_DIR_URL,
				Constants.getDefaultParaFileURL(Constants
						.getLanguageID(Constants.normLanguageName(language))));
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int beamSize) {
		this.beamSize = beamSize;
		this.partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
		setupModules(language, modules, Constants.DEFAULT_WORD_DIR_URL,
				Constants.DEFAULT_SYN_DIR_URL,
				Constants.getDefaultParaFileURL(Constants
						.getLanguageID(Constants.normLanguageName(language))));
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int beamSize, URL wordFileURL) {
		this.beamSize = beamSize;
		this.partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
		setupModules(language, modules, wordFileURL,
				Constants.DEFAULT_SYN_DIR_URL,
				Constants.getDefaultParaFileURL(Constants
						.getLanguageID(Constants.normLanguageName(language))));
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int beamSize, URL wordFileURL,
			URL synDirURL) {
		this.beamSize = beamSize;
		this.partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
		setupModules(language, modules, wordFileURL, synDirURL,
				Constants.getDefaultParaFileURL(Constants
						.getLanguageID(Constants.normLanguageName(language))));
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int beamSize, URL wordFileURL,
			URL synDirURL, URL paraDirURL) {
		this.beamSize = beamSize;
		this.partialComparator = Constants.PARTIAL_COMPARE_TOTAL;
		setupModules(language, modules, wordFileURL, synDirURL, paraDirURL);
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int beamSize, URL wordFileURL,
			URL synDirURL, URL paraDirURL,
			Comparator<PartialAlignment> partialComparator) {
		this.beamSize = beamSize;
		this.partialComparator = partialComparator;
		setupModules(language, modules, wordFileURL, synDirURL, paraDirURL);
		this.moduleWeights = moduleWeights;
	}

	public Aligner(Aligner aligner) {
		this.beamSize = aligner.beamSize;
		this.moduleCount = aligner.moduleCount;
		this.language = aligner.language;
		this.modules = new ArrayList<Integer>(aligner.modules);
		this.moduleWeights = new ArrayList<Double>(aligner.moduleWeights);
		this.partialComparator = aligner.partialComparator;
		for (int module : this.modules) {
			if (module == Constants.MODULE_STEM) {
				// Each aligner needs its own stemmer
				this.stemmer = Constants.newStemmer(this.language);
			} else if (module == Constants.MODULE_SYNONYM) {
				// Dictionaries can be shared
				this.synonyms = aligner.synonyms;
			} else if (module == Constants.MODULE_PARAPHRASE) {
				// Dictionaries can be shared
				this.paraphrase = aligner.paraphrase;
			}
		}
		this.functionWords = aligner.functionWords;
	}

	private void setupModules(String language, ArrayList<Integer> modules,
			URL wordFileURL, URL synDirURL, URL paraDirURL) {
		this.language = Constants.normLanguageName(language);
		this.moduleCount = modules.size();
		this.modules = modules;
		this.moduleWeights = new ArrayList<Double>();
		for (int i = 0; i < this.modules.size(); i++) {
			int module = this.modules.get(i);
			if (module == Constants.MODULE_EXACT) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_EXACT);
			} else if (module == Constants.MODULE_STEM) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_STEM);
				this.stemmer = Constants.newStemmer(this.language);
			} else if (module == Constants.MODULE_SYNONYM) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_SYNONYM);
				try {
					URL excFileURL = new URL(synDirURL.toString() + "/"
							+ this.language + ".exceptions");
					URL synFileURL = new URL(synDirURL.toString() + "/"
							+ this.language + ".synsets");
					URL relFileURL = new URL(synDirURL.toString() + "/"
							+ this.language + ".relations");
					this.synonyms = new SynonymDictionary(excFileURL,
							synFileURL, relFileURL);
				} catch (IOException ex) {
					throw new RuntimeException(
							"Error: Synonym dictionary could not be loaded ("
									+ synDirURL.toString() + ")");
				}
			} else if (module == Constants.MODULE_PARAPHRASE) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_PARAPHRASE);
				this.paraphrase = new ParaphraseTransducer(paraDirURL);
			}
		}
		this.functionWords = new HashSet<String>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					wordFileURL.openStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				this.functionWords.add(line);
			}
			in.close();
		} catch (IOException ex) {
			throw new RuntimeException("No function word list ("
					+ wordFileURL.toString() + ")");
		}
	}

	public void updateModuleWeights(ArrayList<Double> moduleWeights) {
		this.moduleWeights = new ArrayList<Double>(moduleWeights);
	}

	public Alignment align(String line1, String line2) {
		Alignment a = new Alignment(line1, line2);
		align(a);
		return a;
	}

	public Alignment align(ArrayList<String> words1, ArrayList<String> words2) {
		Alignment a = new Alignment(words1, words2);
		align(a);
		return a;
	}

	private void align(Alignment a) {

		// Set the stage for matching
		Stage s = new Stage(a.words1, a.words2);

		// Special case: if sentences are identical, only exact matches are
		// needed. This prevents beam search errors.
		int modsUsed = moduleCount;
		if (a.words1.size() == a.words2.size()
				&& Arrays.equals(s.words1, s.words2)) {
			modsUsed = 1;
		}

		// For each module
		for (int modNum = 0; modNum < modsUsed; modNum++) {

			// Get the matcher for this module
			int matcher = modules.get(modNum);

			// Match with the appropriate module
			if (matcher == Constants.MODULE_EXACT) {
				// Exact just needs the alignment object
				ExactMatcher.match(modNum, a, s);
			} else if (matcher == Constants.MODULE_STEM) {
				// Stem also need the stemmer
				StemMatcher.match(modNum, a, s, stemmer);
			} else if (matcher == Constants.MODULE_SYNONYM) {
				// Synonym also need the synonym dictionary
				SynonymMatcher.match(modNum, a, s, synonyms);
			} else if (matcher == Constants.MODULE_PARAPHRASE) {
				// Paraphrase also need the paraphrase dictionary
				ParaphraseMatcher.match(modNum, a, s, paraphrase);
			} else {
				throw new RuntimeException("Matcher not recognized: " + matcher);
			}
		}

		// All possible matches have been identified. Now search
		// for the highest scoring alignment.

		boolean[] line1UsedWords = new boolean[a.words1.size()];
		Arrays.fill(line1UsedWords, false);

		boolean[] line2UsedWords = new boolean[a.words2.size()];
		Arrays.fill(line2UsedWords, false);

		PartialAlignment initialPath = new PartialAlignment(
				new Match[a.words2.size()], line1UsedWords, line2UsedWords);

		// One-to-one, non-overlapping matches are definite
		for (int i = 0; i < s.matches.size(); i++) {
			if (s.matches.get(i).size() == 1) {
				Match m = s.matches.get(i).get(0);
				boolean overlap = false;
				for (int j = 0; j < m.length; j++)
					if (s.line2Coverage[i + j] != 1)
						overlap = true;
				for (int j = 0; j < m.matchLength; j++)
					if (s.line1Coverage[m.matchStart + j] != 1)
						overlap = true;
				if (!overlap) {
					initialPath.matches[i] = m;
					for (int j = 0; j < m.length; j++)
						initialPath.line2UsedWords[i + j] = true;
					for (int j = 0; j < m.matchLength; j++)
						initialPath.line1UsedWords[m.matchStart + j] = true;
				}
			}
		}

		// Resolve best alignment using remaining matches
		PartialAlignment best = resolve(s, initialPath);

		// Match totals
		int[] contentMatches1 = new int[moduleCount];
		int[] contentMatches2 = new int[moduleCount];
		Arrays.fill(contentMatches1, 0);
		Arrays.fill(contentMatches2, 0);

		int[] functionMatches1 = new int[moduleCount];
		int[] functionMatches2 = new int[moduleCount];
		Arrays.fill(functionMatches1, 0);
		Arrays.fill(functionMatches2, 0);

		// Populate these while summing to avoid rehashing
		boolean[] isFunctionWord1 = new boolean[a.words1.size()];
		boolean[] isFunctionWord2 = new boolean[a.words2.size()];

		// Check for function words
		for (int i = 0; i < a.words1.size(); i++)
			if (functionWords.contains(a.words1.get(i).toLowerCase())) {
				isFunctionWord1[i] = true;
				a.line1FunctionWords.add(i);
			}
		for (int i = 0; i < a.words2.size(); i++)
			if (functionWords.contains(a.words2.get(i).toLowerCase())) {
				isFunctionWord2[i] = true;
				a.line2FunctionWords.add(i);
			}

		// Sum matches by module, word type
		for (int i = 0; i < best.matches.length; i++) {
			Match m = best.matches[i];
			if (m != null) {
				for (int j = 0; j < m.matchLength; j++) {
					if (isFunctionWord1[m.matchStart + j])
						functionMatches1[m.module]++;
					else
						contentMatches1[m.module]++;
				}
				for (int j = 0; j < m.length; j++) {
					if (isFunctionWord2[m.start + j])
						functionMatches2[m.module]++;
					else
						contentMatches2[m.module]++;
				}
			}
		}
		for (int i = 0; i < moduleCount; i++) {
			a.moduleContentMatches1.add(contentMatches1[i]);
			a.moduleContentMatches2.add(contentMatches2[i]);
			a.moduleFunctionMatches1.add(functionMatches1[i]);
			a.moduleFunctionMatches2.add(functionMatches2[i]);
		}

		// Copy best partial to final alignment
		a.matches = Arrays.copyOf(best.matches, best.matches.length);

		// Total matches and chunks
		int[] cc = getCountAndChunks(a.matches);
		a.line1Matches = cc[0];
		a.line2Matches = cc[1];
		a.numChunks = cc[2];

		double avgMatches = ((double) (a.line1Matches + a.line2Matches)) / 2;
		a.avgChunkLength = (a.numChunks > 0) ? avgMatches / a.numChunks : 0;
	}

	// Beam search for best alignment
	private PartialAlignment resolve(Stage s, PartialAlignment start) {

		// Current search path queue
		ArrayList<PartialAlignment> paths = null;
		// Next search path queue
		ArrayList<PartialAlignment> nextPaths = new ArrayList<PartialAlignment>();
		nextPaths.add(start);
		// Proceed left to right
		for (int current = 0; current <= s.matches.size(); current++) {
			// Advance
			paths = nextPaths;
			nextPaths = new ArrayList<PartialAlignment>();

			// Sort possible paths
			Collections.sort(paths, partialComparator);

			// Try as many paths as beam allows
			for (int rank = 0; rank < beamSize && rank < paths.size(); rank++) {

				PartialAlignment path = paths.get(rank);

				// Case: Path is complete
				if (current == s.matches.size()) {
					// Close last chunk
					if (path.lastMatchEnd != -1)
						path.chunks++;
					nextPaths.add(path);
					continue;
				}

				// Case: Current index word is in use
				if (path.line2UsedWords[current] == true) {
					// If this is still part of a match
					if (current < path.idx) {
						// Continue
						nextPaths.add(path);
					}
					// If fixed match
					else if (path.matches[path.idx] != null) {
						Match m = path.matches[path.idx];
						// Add both match sizes times module weight
						path.matchCount++;
						path.matches1 += m.matchLength
								* moduleWeights.get(m.module);
						path.matches2 += m.length * moduleWeights.get(m.module);
						path.allMatches1 += m.matchLength;
						path.allMatches2 += m.length;
						// Not continuous in line1
						if (path.lastMatchEnd != -1
								&& m.matchStart != path.lastMatchEnd) {
							path.chunks++;
						}
						// Advance to end of match + 1
						path.idx = m.start + m.length;
						path.lastMatchEnd = m.matchStart + m.matchLength;
						// Add distance
						path.distance += Math.abs(m.start - m.matchStart);
						// Continue
						nextPaths.add(path);
					}
					continue;
				}

				// Case: Multiple possible matches
				// For each match starting at index start
				ArrayList<Match> matches = s.matches.get(current);
				for (int i = 0; i < matches.size(); i++) {
					Match m = matches.get(i);

					// Check to see if words are unused
					if (path.isUsed(m))
						continue;

					// New path
					PartialAlignment newPath = new PartialAlignment(path);

					// Select m for this start index
					newPath.setUsed(m, true);
					newPath.matches[current] = m;

					// Calculate new stats
					newPath.matchCount++;
					newPath.matches1 += m.matchLength
							* moduleWeights.get(m.module);
					newPath.matches2 += m.length * moduleWeights.get(m.module);
					newPath.allMatches1 += m.matchLength;
					newPath.allMatches2 += m.length;
					if (newPath.lastMatchEnd != -1
							&& m.matchStart != newPath.lastMatchEnd) {
						newPath.chunks++;
					}
					newPath.idx = m.start + m.length;
					newPath.lastMatchEnd = m.matchStart + m.matchLength;
					path.distance += Math.abs(m.start - m.matchStart);

					// Add to queue
					nextPaths.add(newPath);
				}
				// Try skipping this index
				if (path.lastMatchEnd != -1) {
					path.chunks++;
					path.lastMatchEnd = -1;
				}
				path.idx++;
				nextPaths.add(path);
			}
			if (nextPaths.size() == 0) {
				System.err
						.println("Warning: unexpected conditions - skipping matches until possible to continue");
				nextPaths.add(paths.get(0));
			}
		}
		// Return top best path
		Collections.sort(nextPaths, partialComparator);
		return nextPaths.get(0);
	}

	// Count matches and chunks, return int[] { matches1, matches2, chunks }
	private int[] getCountAndChunks(Match[] matches) {
		// Chunks
		int matches1 = 0;
		int matches2 = 0;
		int chunks = 0;
		int idx = 0;
		int lastMatchEnd = -1;
		while (idx < matches.length) {
			Match m = matches[idx];
			// Gap in line2
			if (m == null) {
				// End of chunk
				if (lastMatchEnd != -1) {
					chunks++;
					lastMatchEnd = -1;
				}
				// Advance in line2
				idx++;
			} else {
				// Add both match sizes
				matches1 += m.matchLength;
				matches2 += m.length;
				// Not continuous in line1
				if (lastMatchEnd != -1 && m.matchStart != lastMatchEnd) {
					chunks++;
				}
				// Advance to end of match + 1
				idx = m.start + m.length;
				lastMatchEnd = m.matchStart + m.matchLength;
			}
		}
		// End current open chunk if exists
		if (lastMatchEnd != -1)
			chunks++;
		int[] cc = { matches1, matches2, chunks };
		return cc;
	}
}