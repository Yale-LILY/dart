/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SGMData {
	// Patterns for SGML processing
	private static Pattern r_whitespace = Pattern.compile("\\s",
			Pattern.CASE_INSENSITIVE);
	private static String space = " ";

	/* Instance Variables */

	// One test set includes the cross product of systems and documents
	// Data Format:
	// TestSet -> System -> Document -> Segment -> Text
	public Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, String>>>> testData;
	public Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, String>>>> refData;

	public SGMData() {
		testData = new Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, String>>>>();
		refData = new Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, String>>>>();
	}

	public static void populate(SGMData sgm, String fileName,
			boolean isReference) throws IOException {

		// Choose test or reference data to populate
		Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, String>>>> data;
		if (!isReference) {
			data = sgm.testData;
		} else {
			data = sgm.refData;
		}

		// System -> Document -> Segment -> Text
		Hashtable<String, Hashtable<String, Hashtable<String, String>>> currentSet = null;
		String currentSetID = "";
		// Document -> Segment -> Text
		Hashtable<String, Hashtable<String, String>> currentSys = null;
		String currentSysID = "";
		// Segment -> Text
		Hashtable<String, String> currentDoc = null;
		String currentDocID = "";
		String currentSegID = "";

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), "UTF-8"));
		String line;
		Matcher match;

		while ((line = in.readLine()) != null) {

			// Set header
			// Example: <tstset setid="setname" sysid="system01">
			Pattern r_set = Pattern.compile("<\\s*(tst|ref)set",
					Pattern.CASE_INSENSITIVE);
			if ((match = r_set.matcher(line)).find()) {

				// Set ID (required)
				Pattern r_setid = Pattern.compile("setid\\s*=\\s*\"([^\"]+)\"",
						Pattern.CASE_INSENSITIVE);
				if ((match = r_setid.matcher(line)).find()) {
					currentSetID = match.group(1);
					currentSet = data.get(currentSetID);
					if (currentSet == null) {
						currentSet = new Hashtable<String, Hashtable<String, Hashtable<String, String>>>();
						data.put(currentSetID, currentSet);
					}
				} else {
					throw new IOException("Set ID (setid) required in line: "
							+ line);
				}

				// System ID (optional)
				Pattern r_sysid = Pattern.compile(
						"(sys|ref)id\\s*=\\s*\"([^\"]+)\"",
						Pattern.CASE_INSENSITIVE);
				if ((match = r_sysid.matcher(line)).find()) {
					currentSysID = match.group(2);
					currentSys = currentSet.get(currentSysID);
					if (currentSys == null) {
						currentSys = new Hashtable<String, Hashtable<String, String>>();
						currentSet.put(currentSysID, currentSys);
					}
				}
			}

			// Doc header
			// Example: <doc docid="docname" sysid="system01">
			Pattern r_doc = Pattern.compile("<\\s*doc",
					Pattern.CASE_INSENSITIVE);
			if (r_doc.matcher(line).find()) {

				// System ID (can be omitted if included in set header)
				Pattern r_sysid = Pattern.compile(
						"(sys|ref)id\\s*=\\s*\"([^\"]+)\"",
						Pattern.CASE_INSENSITIVE);
				if ((match = r_sysid.matcher(line)).find()) {
					currentSysID = match.group(2);
					currentSys = currentSet.get(currentSysID);
					if (currentSys == null) {
						currentSys = new Hashtable<String, Hashtable<String, String>>();
						currentSet.put(currentSysID, currentSys);
					}
				} else if (currentSys == null) {
					throw new IOException(
							"System ID (sysid or refid) must be specified in either Set or Doc header: "
									+ line);
				}

				// Doc ID (required)
				Pattern r_docid = Pattern.compile("docid\\s*=\\s*\"([^\"]+)\"",
						Pattern.CASE_INSENSITIVE);
				if ((match = r_docid.matcher(line)).find()) {
					currentDocID = match.group(1);
					currentDoc = currentSys.get(currentDocID);
					if (currentDoc == null) {
						currentDoc = new Hashtable<String, String>();
						currentSys.put(currentDocID, currentDoc);
					}
				} else {
					throw new IOException("Doc ID (docid) required in line: "
							+ line);
				}
			}

			// Seg tag
			// Examples:
			// <seg id="1"> Segment text </seg>
			// <seg id="1"> Multiple lines
			Pattern r_seg = Pattern.compile(
					"<\\s*seg\\s*id\\s*=\\s*\"?([^\">]+?)\"?\\s*>",
					Pattern.CASE_INSENSITIVE);
			if ((match = r_seg.matcher(line)).find()) {
				currentSegID = match.group(1);
			} else {
				// Not a Set, Doc, or Seg tag: ignore
				continue;
			}

			// Collect the data for the segment
			Pattern r_segend = Pattern.compile("<\\s*\\/seg\\s*>",
					Pattern.CASE_INSENSITIVE);
			while (!r_segend.matcher(line).find()) {
				line += in.readLine();
			}

			String lineToMatch = r_whitespace.matcher(line).replaceAll(space);
			String txt = "";

			// Extract text
			Pattern r_txt = Pattern.compile("<\\s*seg.*?>(.*)<\\s*\\/seg\\s*>",
					Pattern.CASE_INSENSITIVE);
			if ((match = r_txt.matcher(lineToMatch)).find())
				txt = match.group(1).trim();
			else {
				throw new IOException("Couldn't read segment from line: "
						+ line);
			}

			// Unescape and store text
			currentDoc.put(currentSegID, Normalizer.unescapeSGML(txt));

			// Uncomment for sanity check
			// System.out.println("Stored: " + currentSetID + ":" + currentSysID
			// + ":" + currentDocID + ":" + currentSegID + " - " + txt);
		}

		in.close();
	}
}