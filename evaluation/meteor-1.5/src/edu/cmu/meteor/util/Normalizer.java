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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This normalizer is based on the Moses tokenizer Rev 3612 with additions to
 * improve MT evaluation accuracy.
 * 
 * Nonbreaking prefix lists taken from Moses Rev 3881.
 * 
 * Additions: quote and dash normalization, lossy stripping of dashes and dots
 * in words. Departures from Moses tokenizer marked with "New".
 */
public class Normalizer {

	// Tokenization patterns

	private static String s_space = " ";

	// Lists of alpha and alphanumeric characters, including Unicode ranges
    //                            Western European         Cyrillic
	private static String alpha = "A-Za-zŠŽšžŸÀ-ÖØ-öø-ž" + "Ѐ-ӿԀ-ԧꙀ-ꙮ꙾-ꚗᴀ-ᵿ";
	private static String alnum = "0-9A-Za-zŠŽšžŸÀ-ÖØ-öø-ž" + "Ѐ-ӿԀ-ԧꙀ-ꙮ꙾-ꚗᴀ-ᵿ";

	// List of special case characters not to wrap in whitespace
	private static Pattern r_sep_other = Pattern.compile("([^" + alnum
			+ "\\s\\.\\'\\`\\,\\-\\‘\\’])");
	private static String s_sep_other = " $1 ";

	private static Pattern r_multi_dot = Pattern.compile("\\.([\\.]+)");
	private static String s_multi_dot = " DOTMULTI$1";
	private static String s_multi_dot2 = "DOTMULTI.";
	private static Pattern r_multi_dot2 = Pattern
			.compile("DOTMULTI\\.([^\\.])");
	private static String s_multi_dot3 = "DOTDOTMULTI $1";
	private static String s_multi_dot4 = "DOTDOTMULTI";
	private static String s_multi_dot5 = "DOTMULTI";
	private static String s_multi_dot6 = ".";

	private static Pattern r_comma = Pattern
			.compile("([^\\p{Digit}])[,]([^\\p{Digit}])");
	private static String s_comma = "$1 , $2";
	private static Pattern r_comma2 = Pattern
			.compile("([\\p{Digit}])[,]([^\\p{Digit}])");
	private static Pattern r_comma3 = Pattern
			.compile("([^\\p{Digit}])[,]([\\p{Digit}])");

	// Single quotes to normalize
	private static Pattern r_quote_norm = Pattern.compile("([`‘’])");
	private static String s_quote_norm = "'";
	// Double quotes to normalize
	private static Pattern r_quote_norm2 = Pattern.compile("([“”]|'')");
	private static String s_quote_norm2 = " \" ";

	// Dashes to normalize
	private static String s_dash_norm = "–";
	private static String s_dash_norm2 = "-";
	private static String s_dash_norm3 = "--";

	private static Pattern r_cont_en = Pattern.compile("([^" + alpha
			+ "])[']([^" + alpha + "])");
	private static String s_cont_en = "$1 ' $2";
	private static Pattern r_cont_en2 = Pattern.compile("([^" + alpha
			+ "\\p{Digit}])[']([" + alpha + "])");
	private static Pattern r_cont_en3 = Pattern.compile("([" + alpha
			+ "])[']([^" + alpha + "])");
	private static Pattern r_cont_en4 = Pattern.compile("([" + alpha
			+ "])[']([" + alpha + "])");
	private static String s_cont_en2 = "$1 '$2";
	// 1990's etc.
	private static Pattern r_cont_en5 = Pattern
			.compile("([\\p{Digit}])[']([s])");

	private static Pattern r_cont_fr = Pattern.compile("([^" + alpha
			+ "])[']([^" + alpha + "])");
	private static String s_cont_fr = "$1 ' $2";
	private static Pattern r_cont_fr2 = Pattern.compile("([^" + alpha
			+ "])[']([" + alpha + "])");
	private static Pattern r_cont_fr3 = Pattern.compile("([" + alpha
			+ "])[']([^" + alpha + "])");
	private static Pattern r_cont_fr4 = Pattern.compile("([" + alpha
			+ "])[']([" + alpha + "])");
	private static String s_cont_fr2 = "$1' $2";

	private static String s_cont_other1 = "'";
	private static String s_cont_other2 = " ' ";

	private static Pattern r_punct_strip = Pattern.compile("[^" + alnum + "]");
	private static String s_punct_strip = " ";

	private static Pattern r_rm_dash = Pattern.compile("([" + alnum
			+ "\\.])[\\-]([" + alnum + "])");
	private static String s_rm_dash = "$1 $2";

    //                                                  Unicode spaces
	private static Pattern r_white = Pattern.compile("[               　 ]+");
	private static String s_white = " ";

	// Nonbreaking prefixes
	private static Hashtable<String, Integer> nbpDict = null;
	private static int nbpLangID = Constants.LANG_OTHER;
	private static String s_nbp = ".";
	private static String s_nbp2 = "";
	private static Pattern r_nbp1 = Pattern.compile("[" + alpha + "]");
	private static Pattern r_nbp2 = Pattern.compile("^[\\p{Lower}]");
	private static Pattern r_nbp3 = Pattern.compile("^[0-9]+");
	private static String s_nbp3 = " .";
	private static String s_nbp4 = " ";

	// Should refactor if intending to normalize lines of alternating languages.
	// This implementation works for the MT Evaluation use case.
	private static Hashtable<String, Integer> nbpList(int langID) {

		if (nbpDict != null && nbpLangID == langID)
			return nbpDict;

		// New nbp list
		nbpDict = new Hashtable<String, Integer>();
		nbpLangID = Constants.LANG_OTHER;

		// Try to load the list
		try {
			URL nbpFileURL = new URL(Constants.DEFAULT_NBP_DIR_URL.toString()
					+ "/" + Constants.getLanguageName(langID) + ".prefixes");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					nbpFileURL.openStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				// Skip empty entries
				if (tok.countTokens() == 0)
					continue;
				String pre = tok.nextToken();
				// Skip comments
				if (pre.startsWith("#"))
					continue;
				// Check type
				int type = Constants.NBP_ANY;
				if (tok.hasMoreTokens()
						&& tok.nextToken().equals("#NUMERIC_ONLY#"))
					type = Constants.NBP_NUM_ONLY;
				nbpDict.put(pre, type);
			}
			in.close();

			// Set language for loaded nbp list
			nbpLangID = langID;
		} catch (Exception ex) {
			System.err
					.println("Error: Nonbreaking prefix list could not be loaded:");
			ex.printStackTrace();
		}

		return nbpDict;
	}

	// Normalization for western languages. Checks if language is non-western
	// (and supported) and handles appropriately
	public static String normalizeLine(String line, int langID,
			boolean keepPunctuation) {

		if (!Constants.isSupported(langID)) {
			System.err
					.println("Error: Pre-process the input files and run Meteor without the -norm option.");
			String lang = "";
			try {
				lang = Constants.getLanguageName(langID);
			} catch (Exception ex) {
				// Unknown language, leave blank
			}
			throw new RuntimeException("No normalizer for language (" + lang
					+ ")");
		}

		// Special handling of non-western languages
		if (langID == Constants.LANG_AR_BW_RED || langID == Constants.LANG_OTHER)
			return normalizeNonWestern(line, keepPunctuation);

		// Wrap line in whitespace
		String workingLine = s_space + line + s_space;

		// Separate all non-alphanumeric characters that aren't special cases
		workingLine = r_sep_other.matcher(workingLine).replaceAll(s_sep_other);

		// Escape multiple dots
		workingLine = r_multi_dot.matcher(workingLine).replaceAll(s_multi_dot);
		while (workingLine.contains(s_multi_dot2)) {
			workingLine = r_multi_dot2.matcher(workingLine).replaceAll(
					s_multi_dot3);
			workingLine = workingLine.replace(s_multi_dot2, s_multi_dot4);
		}

		// Separate commas except within numbers
		workingLine = r_comma.matcher(workingLine).replaceAll(s_comma);
		workingLine = r_comma2.matcher(workingLine).replaceAll(s_comma);
		workingLine = r_comma3.matcher(workingLine).replaceAll(s_comma);

		// New: Normalize quotes
		workingLine = r_quote_norm.matcher(workingLine)
				.replaceAll(s_quote_norm);
		workingLine = r_quote_norm2.matcher(workingLine).replaceAll(
				s_quote_norm2);

		// New: Normalize dashes
		workingLine = workingLine.replace(s_dash_norm, s_dash_norm2);
		workingLine = workingLine.replace(s_dash_norm3, s_dash_norm2);

		// New: Remove dashes between words so systems can get partial credit
		// Find dashes alphanum-alphanum or dot-alphanum (U.S.-based)
		workingLine = r_rm_dash.matcher(workingLine).replaceAll(s_rm_dash);

		// Handle apostrophes
		if (langID == Constants.LANG_EN) {
			// English splits contractions right
			workingLine = r_cont_en.matcher(workingLine).replaceAll(s_cont_en);
			workingLine = r_cont_en2.matcher(workingLine).replaceAll(s_cont_en);
			workingLine = r_cont_en3.matcher(workingLine).replaceAll(s_cont_en);
			workingLine = r_cont_en4.matcher(workingLine)
					.replaceAll(s_cont_en2);
			workingLine = r_cont_en5.matcher(workingLine)
					.replaceAll(s_cont_en2);
		} else if (langID == Constants.LANG_FR) {
			// French splits contractions left
			workingLine = r_cont_fr.matcher(workingLine).replaceAll(s_cont_fr);
			workingLine = r_cont_fr2.matcher(workingLine).replaceAll(s_cont_fr);
			workingLine = r_cont_fr3.matcher(workingLine).replaceAll(s_cont_fr);
			workingLine = r_cont_fr4.matcher(workingLine)
					.replaceAll(s_cont_fr2);
		} else {
			// All others split both
			workingLine = workingLine.replace(s_cont_other1, s_cont_other2);
		}

		// Split words, dots still attached
		StringTokenizer tok = new StringTokenizer(workingLine);
		String[] words = new String[tok.countTokens()];
		for (int i = 0; i < words.length; words[i++] = tok.nextToken())
			;
		StringBuilder sb = new StringBuilder();

		// Rebuild line, breaking dots unless cased as nonbreaking
		Hashtable<String, Integer> nbp = nbpList(langID);
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() > 1 && words[i].endsWith(s_nbp)) {
				String pre = words[i].substring(0, words[i].length() - 1);
				Integer type = nbp.get(pre);
				// Mixed (U.S.)
				if (pre.contains(s_nbp) && r_nbp1.matcher(pre).find()) {
					// New: Drop dots to match undotted output (U.S. -> US)
					sb.append(words[i].replace(s_nbp, s_nbp2));
				}// Always nonbreaking or followed by lowercase (trick to catch
					// unlisted items such as Inc.)
				else if ((type != null && type == Constants.NBP_ANY)
						|| (i < words.length - 1 && r_nbp2
								.matcher(words[i + 1]).find())) {
					// No change
					sb.append(words[i]);
				} // Nonbreaking when followed by number
				else if ((type != null && type == Constants.NBP_NUM_ONLY)
						&& (i < words.length - 1 && (r_nbp3
								.matcher(words[i + 1]).find()))) {
					// No change
					sb.append(words[i]);
				} else {
					// Split dot
					sb.append(pre);
					sb.append(s_nbp3);
				}
			} else {
				// No nbp
				sb.append(words[i]);
			}
			sb.append(s_nbp4);
		}
		workingLine = sb.toString();

		// Unescape multiple dots
		while (workingLine.contains(s_multi_dot4)) {
			workingLine = workingLine.replace(s_multi_dot4, s_multi_dot2);
		}
		workingLine = workingLine.replace(s_multi_dot5, s_multi_dot6);

		// New: Remove punctuation if requested
		if (!keepPunctuation) {
			workingLine = r_punct_strip.matcher(workingLine).replaceAll(
					s_punct_strip);
		}

		// Normalize whitespace
		workingLine = r_white.matcher(workingLine).replaceAll(s_white).trim();

		return workingLine;
	}

	// Symbol ranges excluding comma and dot
	private static Pattern r_punct_nonwest = Pattern
			.compile("([\\!-\\+\\-\\/\\:-\\@\\[-\\`\\{-¿\u060C])");
	private static String s_punct_nonwest = " $1 ";
	private static String s_punct_nonwest2 = " ";
	private static String s_punct_nonwest3 = ".";

	private static Pattern r_dot_nonwest = Pattern
			.compile("([^\\p{Digit}])[\\.]([^\\p{Digit}])");
	private static String s_dot_nonwest = "$1 . $2";
	private static Pattern r_dot_nonwest2 = Pattern
			.compile("([\\p{Digit}])[\\.]([^\\p{Digit}])");
	private static Pattern r_dot_nonwest3 = Pattern
			.compile("([^\\p{Digit}])[\\.]([\\p{Digit}])");

	// For non-western languages, only tokenize western punctuation
	private static String normalizeNonWestern(String line,
			Boolean keepPunctuation) {

		// Wrap line in whitespace
		String workingLine = s_space + line + s_space;

		// Escape multiple dots
		workingLine = r_multi_dot.matcher(workingLine).replaceAll(s_multi_dot);
		while (workingLine.contains(s_multi_dot2)) {
			workingLine = r_multi_dot2.matcher(workingLine).replaceAll(
					s_multi_dot3);
			workingLine = workingLine.replace(s_multi_dot2, s_multi_dot4);
		}

		// Separate commas except within numbers
		workingLine = r_comma.matcher(workingLine).replaceAll(s_comma);
		workingLine = r_comma2.matcher(workingLine).replaceAll(s_comma);
		workingLine = r_comma3.matcher(workingLine).replaceAll(s_comma);

		// Normalize quotes
		workingLine = r_quote_norm.matcher(workingLine)
				.replaceAll(s_quote_norm);
		workingLine = r_quote_norm2.matcher(workingLine).replaceAll(
				s_quote_norm2);

		// Normalize dashes
		workingLine = workingLine.replace(s_dash_norm, s_dash_norm2);
		workingLine = workingLine.replace(s_dash_norm3, s_dash_norm2);

		// Separate dots except within numbers
		workingLine = r_dot_nonwest.matcher(workingLine).replaceAll(
				s_dot_nonwest);
		workingLine = r_dot_nonwest2.matcher(workingLine).replaceAll(
				s_dot_nonwest);
		workingLine = r_dot_nonwest3.matcher(workingLine).replaceAll(
				s_dot_nonwest);

		// Tokenize all other punctuation (last to avoid confusion)
		workingLine = r_punct_nonwest.matcher(workingLine).replaceAll(
				s_punct_nonwest);

		// Remove punctuation if requested
		if (!keepPunctuation) {
			workingLine = r_punct_nonwest.matcher(workingLine).replaceAll(
					s_punct_nonwest2);
			workingLine = workingLine.replace(s_punct_nonwest3,
					s_punct_nonwest2);
		}

		// Unescape multiple dots
		while (workingLine.contains(s_multi_dot4)) {
			workingLine = workingLine.replace(s_multi_dot4, s_multi_dot2);
		}
		workingLine = workingLine.replace(s_multi_dot5, s_multi_dot6);

		// Normalize whitespace
		workingLine = r_white.matcher(workingLine).replaceAll(s_white);
		workingLine = workingLine.trim();

		return workingLine;
	}

	// SGML Patterns

	private static Pattern r_quot = Pattern.compile("&quot;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_apos = Pattern.compile("&apos;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_lt = Pattern.compile("&lt;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_gt = Pattern.compile("&gt;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_amp = Pattern.compile("&amp;",
			Pattern.CASE_INSENSITIVE);

	private static String quot = "\"";
	private static String apos = "'";
	private static String lt = "<";
	private static String gt = ">";
	private static String amp = "&";

	// Call separately for SGML input only
	public static String unescapeSGML(String line) {
		String workingLine = line;
		workingLine = r_quot.matcher(workingLine).replaceAll(quot);
		workingLine = r_apos.matcher(workingLine).replaceAll(apos);
		workingLine = r_lt.matcher(workingLine).replaceAll(lt);
		workingLine = r_gt.matcher(workingLine).replaceAll(gt);
		workingLine = r_amp.matcher(workingLine).replaceAll(amp);
		return workingLine;
	}

	// Test the normalizer
	public static void main(String[] args) throws IOException {

		if (args.length < 2) {
			System.out.println("Usage: Normalizer lang punct");
			System.out.println("where puct is true/false");
			return;
		}

		int langID = Constants.getLanguageID(Constants
				.normLanguageName(args[0]));
		boolean punct = Boolean.parseBoolean(args[1]);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = in.readLine()) != null)
			System.out.println(Normalizer.normalizeLine(line, langID, punct));
	}
}
