/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.scorer;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import edu.cmu.meteor.aligner.Alignment;
import edu.cmu.meteor.util.Constants;

/**
 * Class used to hold several Meteor statistics, including final score
 * 
 */
public class MeteorStats {

	public static final int STATS_LENGTH = 23;

	/* Aggregable statistics */

	public double testLength;
	public double referenceLength;

	public double testFunctionWords;
	public double referenceFunctionWords;

	public double testTotalMatches;
	public double referenceTotalMatches;

	public ArrayList<Double> testStageMatchesContent;
	public ArrayList<Double> referenceStageMatchesContent;

	public ArrayList<Double> testStageMatchesFunction;
	public ArrayList<Double> referenceStageMatchesFunction;

	public double chunks;

	// Different in case of character-based evaluation
	public double testWordMatches;
	public double referenceWordMatches;

	/* Calculated statistics */

	/**
	 * Sums weighted by parameters
	 */
	public double testWeightedMatches;
	public double referenceWeightedMatches;

	public double testWeightedLength;
	public double referenceWeightedLength;

	public double precision;
	public double recall;
	public double f1;
	public double fMean;
	public double fragPenalty;

	/**
	 * Score is required to select the best reference
	 */
	public double score;

	/**
	 * Also keep the underlying alignment if needed
	 */
	public Alignment alignment;

	public MeteorStats() {
		testLength = 0.0;
		referenceLength = 0.0;

		testFunctionWords = 0.0;
		referenceFunctionWords = 0.0;

		testTotalMatches = 0.0;
		referenceTotalMatches = 0.0;

		testStageMatchesContent = new ArrayList<Double>();
		referenceStageMatchesContent = new ArrayList<Double>();

		testStageMatchesFunction = new ArrayList<Double>();
		referenceStageMatchesFunction = new ArrayList<Double>();

		chunks = 0.0;

		testWordMatches = 0.0;
		referenceWordMatches = 0.0;

		testWeightedMatches = 0.0;
		referenceWeightedMatches = 0.0;

		testWeightedLength = 0.0;
		referenceWeightedLength = 0.0;
	}

	/**
	 * Aggregate SS (except score), result stored in this instance
	 * 
	 * @param ss
	 */
	public void addStats(MeteorStats ss) {

		testLength += ss.testLength;
		referenceLength += ss.referenceLength;

		testFunctionWords += ss.testFunctionWords;
		referenceFunctionWords += ss.referenceFunctionWords;

		testTotalMatches += ss.testTotalMatches;
		referenceTotalMatches += ss.referenceTotalMatches;

		int sizeDiff = ss.referenceStageMatchesContent.size()
				- referenceStageMatchesContent.size();
		for (int i = 0; i < sizeDiff; i++) {
			testStageMatchesContent.add(0.0);
			referenceStageMatchesContent.add(0.0);
			testStageMatchesFunction.add(0.0);
			referenceStageMatchesFunction.add(0.0);
		}
		for (int i = 0; i < ss.testStageMatchesContent.size(); i++)
			testStageMatchesContent.set(i, testStageMatchesContent.get(i)
					+ ss.testStageMatchesContent.get(i));
		for (int i = 0; i < ss.referenceStageMatchesContent.size(); i++)
			referenceStageMatchesContent.set(i,
					referenceStageMatchesContent.get(i)
							+ ss.referenceStageMatchesContent.get(i));
		for (int i = 0; i < ss.testStageMatchesFunction.size(); i++)
			testStageMatchesFunction.set(i, testStageMatchesFunction.get(i)
					+ ss.testStageMatchesFunction.get(i));
		for (int i = 0; i < ss.referenceStageMatchesFunction.size(); i++)
			referenceStageMatchesFunction.set(i,
					referenceStageMatchesFunction.get(i)
							+ ss.referenceStageMatchesFunction.get(i));

		if (!(ss.testTotalMatches == ss.testLength
				&& ss.referenceTotalMatches == ss.referenceLength && ss.chunks == 1.0))
			chunks += ss.chunks;

		testWordMatches += ss.testWordMatches;
		referenceWordMatches += ss.referenceWordMatches;

		// Score does not aggregate
	}

	/**
	 * Stats are output in lines:
	 * 
	 * tstLen refLen tstFuncWords refFuncWords stage1tstMatchesContent
	 * stage1refMatchesContent stage1tstMatchesFunction stage1refMatchesFunction
	 * s2tc s2rc s2tf s2rf s3tc s3rc s3tf s3rf s4tc s4rc s4tf s4rf chunks
	 * tstwordMatches refWordMatches
	 * 
	 * ex: 15 14 4 3 6 6 2 2 1 1 0 0 1 1 0 0 2 2 1 1 3 15 14
	 * 
	 * @param delim
	 */
	public String toString(String delim) {
		StringBuilder sb = new StringBuilder();
		sb.append(testLength + delim);
		sb.append(referenceLength + delim);
		sb.append(testFunctionWords + delim);
		sb.append(referenceFunctionWords + delim);
		for (int i = 0; i < Constants.MAX_MODULES; i++) {
			if (i < testStageMatchesContent.size()) {
				sb.append(testStageMatchesContent.get(i) + delim);
				sb.append(referenceStageMatchesContent.get(i) + delim);
				sb.append(testStageMatchesFunction.get(i) + delim);
				sb.append(referenceStageMatchesFunction.get(i) + delim);
			} else {
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
			}
		}
		sb.append(chunks + delim);
		sb.append(testWordMatches + delim);
		sb.append(referenceWordMatches + delim);
		return sb.toString().trim();
	}

	public String toString() {
		return this.toString(" ");
	}

	/**
	 * Use a string from the toString() method to create a MeteorStats object.
	 * 
	 * @param ssString
	 */
	public MeteorStats(String ssString) {
		Scanner s = new Scanner(ssString);

		testLength = s.nextDouble();
		referenceLength = s.nextDouble();

		testFunctionWords = s.nextDouble();
		referenceFunctionWords = s.nextDouble();

		testTotalMatches = 0.0;
		referenceTotalMatches = 0.0;

		testStageMatchesContent = new ArrayList<Double>();
		referenceStageMatchesContent = new ArrayList<Double>();

		testStageMatchesFunction = new ArrayList<Double>();
		referenceStageMatchesFunction = new ArrayList<Double>();

		for (int i = 0; i < Constants.MAX_MODULES; i++) {

			double tstC = s.nextDouble();
			double refC = s.nextDouble();

			testTotalMatches += tstC;
			referenceTotalMatches += refC;

			testStageMatchesContent.add(tstC);
			referenceStageMatchesContent.add(refC);

			double tstF = s.nextDouble();
			double refF = s.nextDouble();

			testTotalMatches += tstF;
			referenceTotalMatches += refF;

			testStageMatchesFunction.add(tstF);
			referenceStageMatchesFunction.add(refF);
		}

		chunks = s.nextDouble();

		testWordMatches = s.nextDouble();
		referenceWordMatches = s.nextDouble();
	}

}
