/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 *
 */

package edu.cmu.meteor.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import org.tartarus.snowball.ext.danishStemmer;
import org.tartarus.snowball.ext.dutchStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.finnishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;
import org.tartarus.snowball.ext.germanStemmer;
import org.tartarus.snowball.ext.hungarianStemmer;
import org.tartarus.snowball.ext.italianStemmer;
import org.tartarus.snowball.ext.norwegianStemmer;
import org.tartarus.snowball.ext.portugueseStemmer;
import org.tartarus.snowball.ext.romanianStemmer;
import org.tartarus.snowball.ext.russianStemmer;
import org.tartarus.snowball.ext.spanishStemmer;
import org.tartarus.snowball.ext.swedishStemmer;
import org.tartarus.snowball.ext.turkishStemmer;

import edu.cmu.meteor.aligner.LookupTableStemmer;
import edu.cmu.meteor.aligner.PartialAlignment;
import edu.cmu.meteor.aligner.SnowballStemmerWrapper;
import edu.cmu.meteor.aligner.Stemmer;

public class Constants {

	/* Version */

	public static final String VERSION = "1.5";

	public static final DecimalFormat minFormat = new DecimalFormat(
			"#.##########");

	/*
	 * Normalizer constants
	 */

	public static final URL DEFAULT_NBP_DIR_URL = ClassLoader
			.getSystemResource("nonbreaking");

	public static final int NBP_NUM_ONLY = 2;
	public static final int NBP_ANY = 1;

	/*
	 * Aligner Constants
	 */

	public static final int MODULE_EXACT = 0;
	public static final int MODULE_STEM = 1;
	public static final int MODULE_SYNONYM = 2;
	public static final int MODULE_PARAPHRASE = 3;

	public static final int MAX_MODULES = 4;

	public static final double DEFAULT_WEIGHT_EXACT = 1.0;
	public static final double DEFAULT_WEIGHT_STEM = 1.0;
	public static final double DEFAULT_WEIGHT_SYNONYM = 1.0;
	public static final double DEFAULT_WEIGHT_PARAPHRASE = 1.0;

	public static final int DEFAULT_BEAM_SIZE = 40;

	public static final URL DEFAULT_STEM_DIR_URL = ClassLoader
			.getSystemResource("stem");

	public static final URL DEFAULT_SYN_DIR_URL = ClassLoader
			.getSystemResource("synonym");

	public static final URL DEFAULT_WORD_DIR_URL = ClassLoader
			.getSystemResource("function");

	/*
	 * Scorer Constants
	 */

	/* Languages */

	public static final int LANG_EN = 0;
	public static final int LANG_CZ = 1;
	public static final int LANG_FR = 2;
	public static final int LANG_ES = 3;
	public static final int LANG_DE = 4;
	public static final int LANG_AR_BW_RED = 5;
	public static final int LANG_PT = 6;
	public static final int LANG_RU = 7;
	public static final int LANG_DA = 8;
	public static final int LANG_RO = 9;
	public static final int LANG_HU = 10;
	public static final int LANG_TR = 11;
	public static final int LANG_FI = 12;
	public static final int LANG_NL = 13;
	public static final int LANG_IT = 14;
	public static final int LANG_NO = 15;
	public static final int LANG_SE = 16;

	public static final int LANG_MAX = 16;
	public static final int LANG_OTHER = 99;

	private static HashSet<Integer> supportedLangIDs = null;

	public static final boolean isSupported(int langID) {
		if (supportedLangIDs == null) {
			supportedLangIDs = new HashSet<Integer>();
			supportedLangIDs.add(LANG_EN);
			supportedLangIDs.add(LANG_CZ);
			supportedLangIDs.add(LANG_FR);
			supportedLangIDs.add(LANG_ES);
			supportedLangIDs.add(LANG_DE);
			supportedLangIDs.add(LANG_RU);
		}
		return supportedLangIDs.contains(langID);
	}

	public static String getLangsString() throws RuntimeException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= LANG_MAX; i++) {
			sb.append(" ");
			sb.append(getLanguageShortName(i));
		}
		return sb.toString().trim();
	}

	/* Adequacy task */
	public static final int TASK_ADQ = 0;
	public static final double PARAM_ADQ[][] = {
			//
			{ 0.75, 1.40, 0.45, 0.70 }, // English
			{ 0.0, 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.0, 0.0, 0.0, 0.0 }, // Russian
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};
	public static final double WEIGHT_ADQ[][] = {
			//
			{ 1.0, 1.0, 0.6, 0.8 }, // English
			{ 0.0, 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.0, 0.0, 0.0, 0.0 }, // Russian
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};

	/* Ranking task */
	public static final int TASK_RANK = 1;
	public static final double PARAM_RANK[][] = {
			//
			{ 0.85, 0.20, 0.60, 0.75 }, // English
			{ 0.95, 0.20, 0.60, 0.80 }, // Czech
			{ 0.90, 1.40, 0.60, 0.65 }, // French
			{ 0.65, 1.30, 0.50, 0.80 }, // Spanish
			{ 0.95, 1.00, 0.55, 0.55 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.75, 1.40, 0.70, 0.50 }, // Russian, copy of LI
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};
	public static final double WEIGHT_RANK[][] = {
			//
			{ 1.0, 0.6, 0.8, 0.6 }, // English
			{ 1.0, 0.4, 0.0, 0.0 }, // Czech
			{ 1.0, 0.2, 0.4, 0.0 }, // French
			{ 1.0, 0.8, 0.6, 0.0 }, // Spanish
			{ 1.0, 0.8, 0.2, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 1.0, 0.5, 0.5, 0.0 }, // Russian, copy of LI
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};

	/* HTER task */
	public static final int TASK_HTER = 2;
	public static final double PARAM_HTER[][] = {
			//
			{ 0.40, 1.50, 0.35, 0.55 }, // English
			{ 0.0, 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.0, 0.0, 0.0, 0.0 }, // Russian
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};
	public static final double WEIGHT_HTER[][] = {
			//
			{ 1.0, 0.2, 0.6, 0.8 }, // English
			{ 0.0, 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.0, 0.0, 0.0, 0.0 }, // Russian
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};

	/* Utility task */
	public static final int TASK_UTIL = 3;
	public static final double PARAM_UTIL[][] = {
			//
			{ 0.65, 0.1, 0.55, 0.65 }, // English
			{ 0.0, 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.0, 0.0, 0.0, 0.0 }, // Russian
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};
	public static final double WEIGHT_UTIL[][] = {
			//
			{ 1.0, 0.0, 0.0, 0.8 }, // English
			{ 0.0, 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0, 0.0 }, // Arabic Buckwalter Reduced
			{ 0.0, 0.0, 0.0, 0.0 }, // Portuguese
			{ 0.0, 0.0, 0.0, 0.0 }, // Russian
			{ 0.0, 0.0, 0.0, 0.0 }, // Danish
			{ 0.0, 0.0, 0.0, 0.0 }, // Romanian
			{ 0.0, 0.0, 0.0, 0.0 }, // Hungarian
			{ 0.0, 0.0, 0.0, 0.0 }, // Turkish
			{ 0.0, 0.0, 0.0, 0.0 }, // Finnish
			{ 0.0, 0.0, 0.0, 0.0 }, // Dutch
			{ 0.0, 0.0, 0.0, 0.0 }, // Italian
			{ 0.0, 0.0, 0.0, 0.0 }, // Norwegian
			{ 0.0, 0.0, 0.0, 0.0 }, // Swedish
	};

	public static final int TASK_DEFAULT = TASK_RANK;

	/* Language-independent task */
	public static final int TASK_LI = 99;
	// These parameters tend to work well across languages for which judgment
	// data exists
	public static final double PARAM_I[] = { 0.75, 1.40, 0.70, 0.50 };
	// Non-exact matches all get half credit
	public static final double WEIGHT_I[] = { 1.0, 0.5, 0.5, 0.5 };

	/* Tuning task */
	public static final int TASK_TUNE = 100;
	public static final double PARAM_TUNE[] = { 0.5, 1.0, 0.5, 0.5 };
	public static final double WEIGHT_TUNE[] = { 1.0, 0.5, 0.5, 0.5 };

	/* Universal task */
	public static final int TASK_UNIVERSAL = 101;
	public static final double PARAM_U[] = { 0.70, 1.40, 0.30, 0.70 };
	public static final double WEIGHT_U[] = { 1.0, 0.6, 0, 0 };

	// Cannot be used to set task, only used when options are specified manually
	public static final int TASK_CUSTOM = -1;

	/* Normalization */
	public static final int NO_NORMALIZE = 0;
	public static final int NORMALIZE_LC_ONLY = 1;
	public static final int NORMALIZE_KEEP_PUNCT = 2;
	public static final int NORMALIZE_NO_PUNCT = 3;

	/*
	 * Methods to look up constants
	 */

	public static int getDefaultTask(int langID) {
		// Supported languages get the default task
		if (isSupported(langID)) {
			return TASK_DEFAULT;
		}
		// Unsupported languages get language-independent parameters
		return TASK_LI;
	}

	public static String getLocation() {
		File codeDir = new File(Constants.class.getProtectionDomain()
				.getCodeSource().getLocation().getFile());
		// Class is either in JAR file or build directory
		return codeDir.getParent();
	}

	public static URL getDefaultParaFileURL(int langID) throws RuntimeException {
		String shortLang = getLanguageShortName(langID);
		String fileName = "/data/paraphrase-" + shortLang + ".gz";
		try {
			return new File(getLocation() + fileName).toURI().toURL();
		} catch (MalformedURLException ex) {
			throw new RuntimeException();
		}
	}

	public static URL getDefaultWordFileURL(int langID) throws RuntimeException {
		String lang = getLanguageName(langID);
		try {
			return new URL(DEFAULT_WORD_DIR_URL.toString() + "/" + lang
					+ ".words");
		} catch (MalformedURLException ex) {
			throw new RuntimeException();
		}

	}

	public static String normLanguageName(String language)
			throws RuntimeException {
		String lang = language.toLowerCase();
		if (lang.equals("english") || lang.equals("en"))
			return "english";
		if (lang.equals("czech") || lang.equals("cz") || lang.equals("cs"))
			return "czech";
		if (lang.equals("french") || lang.equals("fr"))
			return "french";
		if (lang.equals("german") || lang.equals("de"))
			return "german";
		if (lang.equals("spanish") || lang.equals("es"))
			return "spanish";
		if (lang.equals("arabic-buckwalter-reduced")
				|| lang.equals("ar-bw-red"))
			return "arabic-buckwalter-reduced";
		if (lang.equals("portuguese") || lang.equals("pt"))
			return "portuguese";
		if (lang.equals("russian") || lang.equals("ru"))
			return "russian";
		if (lang.equals("danish") || lang.equals("da"))
			return "danish";
		if (lang.equals("romanian") || lang.equals("ro"))
			return "romanian";
		if (lang.equals("hungarian") || lang.equals("hu"))
			return "hungarian";
		if (lang.equals("turkish") || lang.equals("tr"))
			return "turkish";
		if (lang.equals("finnish") || lang.equals("fi"))
			return "finnish";
		if (lang.equals("dutch") || lang.equals("nl"))
			return "dutch";
		if (lang.equals("italian") || lang.equals("it"))
			return "italian";
		if (lang.equals("norwegian") || lang.equals("no"))
			return "norwegian";
		if (lang.equals("swedish") || lang.equals("se") || lang.equals("sv"))
			return "swedish";
		if (lang.equals("other") || lang.equals("xx"))
			return "other";
		// Not listed
		throw new RuntimeException("Unknown language (" + language + ")");
	}

	public static int getLanguageID(String language) throws RuntimeException {
		if (language.equals("english"))
			return LANG_EN;
		if (language.equals("czech"))
			return LANG_CZ;
		if (language.equals("french"))
			return LANG_FR;
		if (language.equals("spanish"))
			return LANG_ES;
		if (language.equals("german"))
			return LANG_DE;
		if (language.equals("arabic-buckwalter-reduced"))
			return LANG_AR_BW_RED;
		if (language.equals("portuguese"))
			return LANG_PT;
		if (language.equals("russian"))
			return LANG_RU;
		if (language.equals("danish"))
			return LANG_DA;
		if (language.equals("romanian"))
			return LANG_RO;
		if (language.equals("hungarian"))
			return LANG_HU;
		if (language.equals("turkish"))
			return LANG_TR;
		if (language.equals("finnish"))
			return LANG_FI;
		if (language.equals("dutch"))
			return LANG_NL;
		if (language.equals("italian"))
			return LANG_IT;
		if (language.equals("norwegian"))
			return LANG_NO;
		if (language.equals("swedish"))
			return LANG_SE;
		if (language.equals("other"))
			return LANG_OTHER;
		// Not found
		throw new RuntimeException("Unknown language (" + language + ")");
	}

	public static String getLanguageName(int langID) throws RuntimeException {
		if (langID == LANG_EN)
			return "english";
		if (langID == LANG_CZ)
			return "czech";
		if (langID == LANG_FR)
			return "french";
		if (langID == LANG_ES)
			return "spanish";
		if (langID == LANG_DE)
			return "german";
		if (langID == LANG_AR_BW_RED)
			return "arabic-buckwalter-reduced";
		if (langID == LANG_PT)
			return "portuguese";
		if (langID == LANG_RU)
			return "russian";
		if (langID == LANG_DA)
			return "danish";
		if (langID == LANG_RO)
			return "romanian";
		if (langID == LANG_HU)
			return "hungarian";
		if (langID == LANG_TR)
			return "turkish";
		if (langID == LANG_FI)
			return "finnish";
		if (langID == LANG_NL)
			return "dutch";
		if (langID == LANG_IT)
			return "italian";
		if (langID == LANG_NO)
			return "norwegian";
		if (langID == LANG_SE)
			return "swedish";
		if (langID == LANG_OTHER)
			return "other";
		// Not found
		throw new RuntimeException("Unknown language ID (" + langID + ")");
	}

	public static String getLanguageShortName(int langID)
			throws RuntimeException {
		if (langID == LANG_EN)
			return "en";
		if (langID == LANG_CZ)
			return "cz";
		if (langID == LANG_FR)
			return "fr";
		if (langID == LANG_ES)
			return "es";
		if (langID == LANG_DE)
			return "de";
		if (langID == LANG_AR_BW_RED)
			return "ar-bw-red";
		if (langID == LANG_PT)
			return "pt";
		if (langID == LANG_RU)
			return "ru";
		if (langID == LANG_DA)
			return "da";
		if (langID == LANG_RO)
			return "ro";
		if (langID == LANG_HU)
			return "hu";
		if (langID == LANG_TR)
			return "tr";
		if (langID == LANG_FI)
			return "fi";
		if (langID == LANG_NL)
			return "nl";
		if (langID == LANG_IT)
			return "it";
		if (langID == LANG_NO)
			return "no";
		if (langID == LANG_SE)
			return "se";
		if (langID == LANG_OTHER)
			return "other";
		// Not found
		throw new RuntimeException("Unknown language ID (" + langID + ")");
	}

	public static String getNormName(int norm) throws RuntimeException {
		if (norm == NO_NORMALIZE)
			return "no_norm";
		if (norm == NORMALIZE_LC_ONLY)
			return "lc_only";
		if (norm == NORMALIZE_KEEP_PUNCT)
			return "norm";
		if (norm == NORMALIZE_NO_PUNCT)
			return "norm_nopunct";
		// Not found
		throw new RuntimeException("Unknown normalization type (" + norm + ")");
	}

	public static int getModuleID(String modName) throws RuntimeException {
		String mod = modName.toLowerCase();
		if (mod.equals("exact"))
			return MODULE_EXACT;
		if (mod.equals("stem"))
			return MODULE_STEM;
		if (mod.equals("synonym"))
			return MODULE_SYNONYM;
		if (mod.equals("paraphrase"))
			return MODULE_PARAPHRASE;
		// Not found
		throw new RuntimeException("Unknown module (" + modName + ")");
	}

	public static String getModuleName(int module) throws RuntimeException {
		if (module == MODULE_EXACT)
			return "exact";
		if (module == MODULE_STEM)
			return "stem";
		if (module == MODULE_SYNONYM)
			return "synonym";
		if (module == MODULE_PARAPHRASE)
			return "paraphrase";
		// Not found
		throw new RuntimeException("Unknown module ID (" + module + ")");
	}

	public static String getModuleShortName(int module) throws RuntimeException {
		if (module == MODULE_EXACT)
			return "ex";
		if (module == MODULE_STEM)
			return "st";
		if (module == MODULE_SYNONYM)
			return "sy";
		if (module == MODULE_PARAPHRASE)
			return "pa";
		// Not found
		throw new RuntimeException("Unknown module ID (" + module + ")");
	}

	public static String getModuleListString(ArrayList<Integer> mods)
			throws RuntimeException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mods.size(); i++) {
			sb.append(getModuleShortName(mods.get(i)));
			if (i < mods.size() - 1)
				sb.append("_");
		}
		return sb.toString();
	}

	public static int getTaskID(String taskName) throws RuntimeException {
		String task = taskName.toLowerCase();
		if (task.equals("default"))
			return TASK_DEFAULT;
		if (task.equals("adq"))
			return TASK_ADQ;
		if (task.equals("rank"))
			return TASK_RANK;
		if (task.equals("hter"))
			return TASK_HTER;
		if (task.equals("li"))
			return TASK_LI;
		if (task.equals("tune"))
			return TASK_TUNE;
		if (task.equals("util"))
			return TASK_UTIL;
		if (task.equals("universal"))
			return TASK_UNIVERSAL;
		if (task.startsWith("custom"))
			return TASK_CUSTOM;
		// Not found
		throw new RuntimeException("Unknown task (" + taskName + ")");
	}

	public static String getTaskName(int task) throws RuntimeException {
		if (task == TASK_ADQ)
			return "adq";
		if (task == TASK_RANK)
			return "rank";
		if (task == TASK_HTER)
			return "hter";
		if (task == TASK_LI)
			return "li";
		if (task == TASK_TUNE)
			return "tune";
		if (task == TASK_UTIL)
			return "util";
		if (task == TASK_UNIVERSAL)
			return "universal";
		if (task == TASK_CUSTOM)
			return "custom";
		// Not found
		throw new RuntimeException("Unknown task ID (" + task + ")");
	}

	public static String getTaskDescription(String task)
			throws RuntimeException {
		return getTaskDescription(getTaskID(task));
	}

	public static String getTaskDescription(int task) throws RuntimeException {
		if (task == TASK_ADQ)
			return "Adequacy";
		if (task == TASK_RANK)
			return "Ranking";
		if (task == TASK_HTER)
			return "HTER";
		if (task == TASK_LI)
			return "Language-Independent";
		if (task == TASK_TUNE)
			return "Tune";
		if (task == TASK_UTIL)
			return "Translator-Utility";
		if (task == TASK_UNIVERSAL)
			return "Universal";
		if (task == TASK_CUSTOM)
			return "Custom";
		// Not found
		throw new RuntimeException("Unknown task ID (" + task + ")");
	}

	public static ArrayList<Double> getParameters(int langID, int taskID) {

		// Get task
		double[] TASK_PARAM;
		// Order: universal, other/li, lang
		if (taskID == TASK_UNIVERSAL) {
			TASK_PARAM = PARAM_U;
		} else if (langID == LANG_OTHER || taskID == TASK_LI) {
			TASK_PARAM = PARAM_I;
		} else if (taskID == TASK_TUNE) {
			TASK_PARAM = PARAM_TUNE;
		} else if (taskID == TASK_RANK) {
			TASK_PARAM = PARAM_RANK[langID];
		} else if (taskID == TASK_HTER) {
			TASK_PARAM = PARAM_HTER[langID];
		} else if (taskID == TASK_UTIL) {
			TASK_PARAM = PARAM_UTIL[langID];
		} else {
			// Assume TASK_ADQ
			TASK_PARAM = PARAM_ADQ[langID];
		}

		// Copy parameters
		ArrayList<Double> parameters = new ArrayList<Double>();
		for (double param : TASK_PARAM)
			parameters.add(param);

		return parameters;
	}

	public static ArrayList<Double> getModuleWeights(String language,
			String task) throws RuntimeException {
		return getModuleWeights(getLanguageID(language), getTaskID(task));
	}

	public static ArrayList<Double> getModuleWeights(int langID, int taskID) {

		// Get task
		double[] TASK_WEIGHT;
		// Order: universal, other/li, lang
		if (taskID == TASK_UNIVERSAL) {
			TASK_WEIGHT = WEIGHT_U;
		} else if (langID == LANG_OTHER || taskID == TASK_LI) {
			TASK_WEIGHT = WEIGHT_I;
		} else if (taskID == TASK_TUNE) {
			TASK_WEIGHT = WEIGHT_TUNE;
		} else if (taskID == TASK_RANK) {
			TASK_WEIGHT = WEIGHT_RANK[langID];
		} else if (taskID == TASK_HTER) {
			TASK_WEIGHT = WEIGHT_HTER[langID];
		} else if (taskID == TASK_UTIL) {
			TASK_WEIGHT = WEIGHT_UTIL[langID];
		} else {
			// Assume TASK_ADQ
			TASK_WEIGHT = WEIGHT_ADQ[langID];
		}

		// Copy weights
		ArrayList<Double> weights = new ArrayList<Double>();
		for (double weight : TASK_WEIGHT)
			weights.add(weight);

		return weights;
	}

	public static String getWeightListString(ArrayList<Double> weights) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < weights.size(); i++) {
			sb.append(weights.get(i));
			if (i < weights.size() - 1)
				sb.append("_");
		}
		return sb.toString();
	}

	public static Stemmer newStemmer(String language) throws RuntimeException {
		if (language.equals("english"))
			return new SnowballStemmerWrapper(new englishStemmer());
		if (language.equals("french"))
			return new SnowballStemmerWrapper(new frenchStemmer());
		if (language.equals("german"))
			return new SnowballStemmerWrapper(new germanStemmer());
		if (language.equals("spanish"))
			return new SnowballStemmerWrapper(new spanishStemmer());
		if (language.equals("portuguese"))
			return new SnowballStemmerWrapper(new portugueseStemmer());
		if (language.equals("russian"))
			return new SnowballStemmerWrapper(new russianStemmer());
		if (language.equals("danish"))
			return new SnowballStemmerWrapper(new danishStemmer());
		if (language.equals("romanian"))
			return new SnowballStemmerWrapper(new romanianStemmer());
		if (language.equals("hungarian"))
			return new SnowballStemmerWrapper(new hungarianStemmer());
		if (language.equals("turkish"))
			return new SnowballStemmerWrapper(new turkishStemmer());
		if (language.equals("finnish"))
			return new SnowballStemmerWrapper(new finnishStemmer());
		if (language.equals("dutch"))
			return new SnowballStemmerWrapper(new dutchStemmer());
		if (language.equals("italian"))
			return new SnowballStemmerWrapper(new italianStemmer());
		if (language.equals("norwegian"))
			return new SnowballStemmerWrapper(new norwegianStemmer());
		if (language.equals("swedish"))
			return new SnowballStemmerWrapper(new swedishStemmer());
		if (language.equals("arabic-buckwalter-reduced")) {
			try {
				return new LookupTableStemmer(new URL(
						DEFAULT_STEM_DIR_URL.toString() + "/" + language
								+ ".gz"));
			} catch (IOException ex) {
				throw new RuntimeException(
						"Error loading stemmer for language (" + language + ")");
			}
		}
		// Not found
		throw new RuntimeException("No stemmer for language (" + language + ")");
	}

	public static ArrayList<Integer> getModules(int langID, int taskID)
			throws RuntimeException {
		ArrayList<Integer> modules = new ArrayList<Integer>();
		// Universal overrised language
		if (taskID == TASK_UNIVERSAL) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_EN) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
			modules.add(MODULE_SYNONYM);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_FR) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_ES) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_DE) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_CZ) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_AR_BW_RED) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_PT) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_RU) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
			modules.add(MODULE_PARAPHRASE);
		} else if (langID == LANG_DA) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_RO) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_HU) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_TR) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_FI) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_NL) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_IT) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_NO) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_SE) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (langID == LANG_OTHER) {
			modules.add(MODULE_EXACT);
		} else {
			// Not found
			String lang = "";
			String task = "";
			try {
				lang = getLanguageName(langID);
				task = getTaskName(taskID);
			} catch (Exception ex) {
				// Blank lang and task
			}
			throw new RuntimeException(
					"No default modules for language and task (" + lang + ") ("
							+ task + ")");
		}
		return modules;
	}

	// Compare partial alignments: consider total only
	// Use with exact, stem, paraphrase weights 1 0.5 0.5 0.5
	// Use with full paraphrase table
	public static Comparator<PartialAlignment> PARTIAL_COMPARE_TOTAL = new Comparator<PartialAlignment>() {
		public int compare(PartialAlignment x, PartialAlignment y) {
			// More matches always wins
			int matchDiff = (y.matches1 + y.matches2)
					- (x.matches1 + x.matches2);
			if (matchDiff > 0) {
				return 1;
			}
			if (matchDiff < 0) {
				return -1;
			}
			// Otherwise fewer chunks wins
			int chunkDiff = x.chunks - y.chunks;
			if (chunkDiff != 0)
				return chunkDiff;
			// Finally shortest distance wins
			return x.distance - y.distance;
		}
	};

	// Compare partial alignments: consider total, then all matches
	// Use with exact, stem, paraphrase weights 1 1 1 0
	// Use with filtered paraphrase table
	public static Comparator<PartialAlignment> PARTIAL_COMPARE_TOTAL_ALL = new Comparator<PartialAlignment>() {
		public int compare(PartialAlignment x, PartialAlignment y) {
			// 2 - Words covered by exact matches (more)
			int matchDiff = (y.matches1 + y.matches2)
					- (x.matches1 + x.matches2);
			if (matchDiff > 0) {
				return 1;
			}
			if (matchDiff < 0) {
				return -1;
			}
			// 3 - Total words covered (more)
			int allMatchDiff = (y.allMatches1 + y.allMatches2)
					- (x.allMatches1 + x.allMatches2);
			if (allMatchDiff > 0) {
				return 1;
			}
			if (allMatchDiff < 0) {
				return -1;
			}
			// 4 - Number of chunks (less)
			int chunkDiff = x.chunks - y.chunks;
			if (chunkDiff != 0)
				return chunkDiff;
			// 5 - Absolute match distance (less)
			return x.distance - y.distance;
		}
	};
}
