package edu.cmu.meteor.aligner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

public class LookupTableStemmer implements Stemmer {

	Hashtable<Integer, String> table = new Hashtable<Integer, String>();

	public LookupTableStemmer(URL lookupFileURL) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(lookupFileURL.openStream()), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				String word = tok.nextToken();
				// Counts aren't currently used. Right now we just take the most
				// likely stem
				int count = Integer.parseInt(tok.nextToken());
				String stem = tok.nextToken();
				table.put(word.hashCode(), stem);
			}
		} catch (FileNotFoundException fe) {
			throw new RuntimeException("Error: file not found ("
					+ lookupFileURL + ")");
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public String stem(String word) {
		String result = table.get(word.hashCode());
		if (result != null)
			return result;
		return word;
	}
}
