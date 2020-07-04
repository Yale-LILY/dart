/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import edu.cmu.meteor.util.SGMData;

public class SGMtoPlaintext {
	public static void main(String[] args) throws IOException {

		if (args.length < 2) {
			System.out.println("Convert SGML files to plaintext");
			System.out.println();
			System.out.println("Usage: java -cp meteor-*.jar SGMtoPlaintext"
					+ " <test.sgm> <reference.sgm>");
			System.exit(1);
		}

		String testFile = args[0];
		String refFile = args[1];

		// Gather SGML data
		SGMData data = new SGMData();
		SGMData.populate(data, testFile, false);
		SGMData.populate(data, refFile, true);

		// Collect all keys in references
		Hashtable<String, Hashtable<String, HashSet<String>>> refSetKeys = new Hashtable<String, Hashtable<String, HashSet<String>>>();
		Iterator<String> setIDs = data.refData.keySet().iterator();
		while (setIDs.hasNext()) {
			String setID = setIDs.next();
			System.out.println("Found reference set [" + setID + "]");
			Hashtable<String, HashSet<String>> refDocKeys = refSetKeys
					.get(setID);
			if (refDocKeys == null) {
				refDocKeys = new Hashtable<String, HashSet<String>>();
				refSetKeys.put(setID, refDocKeys);
			}
			Hashtable<String, Hashtable<String, Hashtable<String, String>>> refs = data.refData
					.get(setID);
			Iterator<String> refIDs = refs.keySet().iterator();
			while (refIDs.hasNext()) {
				String refID = refIDs.next();
				System.out.println("Found reference [" + refID + "]");
				Hashtable<String, Hashtable<String, String>> docs = refs
						.get(refID);
				Iterator<String> docIDs = docs.keySet().iterator();
				while (docIDs.hasNext()) {
					String docID = docIDs.next();
					HashSet<String> refSegKeys = refDocKeys.get(docID);
					if (refSegKeys == null) {
						refSegKeys = new HashSet<String>();
						refDocKeys.put(docID, refSegKeys);
					}
					Hashtable<String, String> segs = docs.get(docID);
					refSegKeys.addAll(segs.keySet());
				}
			}
		}

		// Sort keys for plaintext output
		AssocList refSetKeyList = new AssocList();
		setIDs = refSetKeys.keySet().iterator();
		while (setIDs.hasNext()) {
			String setID = setIDs.next();
			AssocList refDocKeyList = refSetKeyList.addAndGet(setID);
			Hashtable<String, HashSet<String>> refDocKeys = refSetKeys
					.get(setID);
			Iterator<String> docIDs = refDocKeys.keySet().iterator();
			while (docIDs.hasNext()) {
				String docID = docIDs.next();
				AssocList refSegKeyList = refDocKeyList.addAndGet(docID);
				HashSet<String> refSegKeys = refDocKeys.get(docID);
				Iterator<String> segIDs = refSegKeys.iterator();
				while (segIDs.hasNext()) {
					String segID = segIDs.next();
					refSegKeyList.addAndGet(segID);
					// End segs: sort
					Collections.sort(refSegKeyList.keyList,
							new Comparator<String>() {
								public int compare(String seg1, String seg2) {
									try {
										// Try to compare as ints
										int seg1int = Integer.parseInt(seg1);
										int seg2int = Integer.parseInt(seg2);
										return seg1int - seg2int;
									} catch (Exception ex) {
										// Otherwise compare as strings
										return seg1.compareTo(seg2);
									}
								}
							});
				}
				// End docs: sort
				Collections.sort(refDocKeyList.keyList);
			}
			// End sets: sort
			Collections.sort(refSetKeyList.keyList);
		}

		/*
		 * Test files
		 */
		// For set
		for (String setID : refSetKeyList.keyList) {
			System.out.println("Found test set [" + setID + "]");
			Hashtable<String, Hashtable<String, Hashtable<String, String>>> testSyss = data.testData
					.get(setID);
			// For sys
			Iterator<String> sysIDs = testSyss.keySet().iterator();
			while (sysIDs.hasNext()) {
				String sysID = sysIDs.next();
				System.out.println("Found system [" + sysID + "]");
				PrintWriter out = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(setID + "." + sysID + ".tst"),
						"UTF-8"));
				Hashtable<String, Hashtable<String, String>> testDocs = testSyss
						.get(sysID);
				AssocList refDocKeyList = refSetKeyList.get(setID);
				for (String docID : refDocKeyList.keyList) {
					Hashtable<String, String> testSegs = testDocs.get(docID);
					AssocList refSegKeyList = refDocKeyList.get(docID);
					if (testSegs == null) {
						System.err.println("Warning: no document [" + docID
								+ "] for system [" + sysID
								+ "] - writing blank lines");
					}
					for (String segID : refSegKeyList.keyList) {
						if (testSegs != null) {
							String txt = testSegs.get(segID);
							if (txt != null) {
								out.println(txt);
							} else {
								// Null segment
								System.err.println("Warning: no segment ["
										+ docID + "][" + segID
										+ "] for system [" + sysID
										+ "] - writing blank line");
								out.println();
							}
						} else {
							// Null document
							out.println();
						}
					}
				}
				out.close();
			}
		}

		// For set
		for (String setID : refSetKeyList.keyList) {
			Hashtable<String, Hashtable<String, Hashtable<String, String>>> refSyss = data.refData
					.get(setID);
			// For sys
			Iterator<String> sysIDs = refSyss.keySet().iterator();
			while (sysIDs.hasNext()) {
				String sysID = sysIDs.next();
				PrintWriter out = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(setID + "." + sysID + ".ref"),
						"UTF-8"));
				Hashtable<String, Hashtable<String, String>> refDocs = refSyss
						.get(sysID);
				AssocList refDocKeyList = refSetKeyList.get(setID);
				for (String docID : refDocKeyList.keyList) {
					Hashtable<String, String> refSegs = refDocs.get(docID);
					AssocList refSegKeyList = refDocKeyList.get(docID);
					if (refSegs == null) {
						System.err.println("Warning: no document [" + docID
								+ "] for reference [" + sysID
								+ "] - writing blank lines");
					}
					for (String segID : refSegKeyList.keyList) {
						if (refSegs != null) {
							String txt = refSegs.get(segID);
							if (txt != null) {
								out.println(txt);
							} else {
								// Null segment
								System.err.println("Warning: no segment ["
										+ docID + "][" + segID
										+ "] for reference [" + sysID
										+ "] - writing blank line");
								out.println();
							}
						} else {
							// Null document
							out.println();
						}
					}
				}
				out.close();
			}
		}

	}

	static class AssocList {

		ArrayList<String> keyList = new ArrayList<String>();
		Hashtable<String, AssocList> next = new Hashtable<String, AssocList>();

		AssocList addAndGet(String s) {
			AssocList al = new AssocList();
			keyList.add(s);
			next.put(s, al);
			return al;
		}

		AssocList get(String s) {
			return next.get(s);
		}

		void print(String pre) {
			for (String s : keyList) {
				System.out.println(pre + s);
				AssocList al = next.get(s);
				if (al != null)
					al.print(pre + " ");
			}
		}

		void print() {
			print("");
		}
	}
}
