/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.aligner;

public class Match {

	public int start; // start of the match (line2)
	public int length; // length of this match (line2)
	public int matchStart; // start of this match (line1)
	public int matchLength; // length of this match (line1)
	public double prob; // probability supplied by matcher
	public int module; // module which made this match

	public String toString() {
		return start + ":" + length + " " + matchStart + ":" + matchLength;
	}
}