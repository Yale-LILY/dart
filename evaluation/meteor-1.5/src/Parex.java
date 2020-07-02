import java.io.File;
import java.text.DecimalFormat;

import edu.cmu.parex.ParaphraseExtractor;

/**
 * Main class for paraphrase extraction
 * 
 */

public class Parex {
	public static void main(String[] args) throws Exception {

		if (args.length < 5) {
			printUsage();
			System.exit(0);
		}

		// Corpus
		String fCorpus = args[0];
		String eCorpus = args[1];
		// Phrase table
		String pt = args[2];
		// Target corpus
		String eTgtCorpus = args[3];
		// Output directory
		File outDir = new File(args[4]);
		// Minimum translation prob
		double minPhraseProb = ParaphraseExtractor.MIN_TRANS_PROB;
		if (args.length > 5)
			minPhraseProb = Double.parseDouble(args[5]);
		// Minimum word relative frequency
		double minRF = ParaphraseExtractor.MIN_REL_FREQ;
		if (args.length > 6)
			minRF = Double.parseDouble(args[6]);
		// Minimum final probability
		double minFinalProb = ParaphraseExtractor.MIN_FINAL_PROB;
		if (args.length > 7)
			minRF = Double.parseDouble(args[7]);
		// Symbol String
		String symbols = ParaphraseExtractor.SYMBOLS;
		if (args.length > 8)
			symbols = args[8];

		// Setup
		if (outDir.exists()) {
			System.err
					.println("Directory exists, exiting: " + outDir.getPath());
			System.exit(1);
		}
		outDir.mkdir();
		String outPrefix = (new File(outDir, "parex")).getPath();

		// Step 1: Find common words
		System.err.println("Step 1: building common word lists");
		System.err.println("+ foreign");
		String fCommon = outPrefix + ".f";
		ParaphraseExtractor.findCommonWords(fCorpus, fCommon, minRF);
		System.err.println("+ english");
		String eCommon = outPrefix + ".e";
		ParaphraseExtractor.findCommonWords(eCorpus, eCommon, minRF);

		// Step 2: Extracting paraphrases
		System.err.println("Step 2: extracting paraphrases");
		String raw = outPrefix + ".raw.gz";
		ParaphraseExtractor.extractParaphrases(eTgtCorpus, pt, fCommon,
				eCommon, raw, minPhraseProb, symbols);

		// Step 3: Group paraphrases
		System.err.println("Step 3: grouping paraphrases");
		String group = outPrefix + ".group.gz";
		ParaphraseExtractor.groupParaphrases(raw, group);

		// Step 4: Combine paraphrases
		System.err.println("Step 4: combining and filtering paraphrases");
		String par = (new File(outDir, "paraphrase.gz")).getPath();
		ParaphraseExtractor.combineParaphrases(group, par, minFinalProb);
	}

	public static void printUsage() {
		DecimalFormat df = new DecimalFormat("0.##########");
		System.err.println("Paraphrase Extractor");
		System.err.println();
		System.err
				.println("Usage: java -XX:+UseCompressedOops -Xmx12G -cp meteor-*.jar Parex <fCorpus> <eCorpus> <phrase-table.gz> <eTgtCorpus> <outDir> [minTP] [minRF] [minFinalP] [symbols]");
		System.err.println();
		System.err.println("Args:");
		System.err.println("<fCorpus> foreign corpus");
		System.err.println("<eCorpus> english corpus");
		System.err
				.println("<phrase-table.gz> phrasetable built from corpora (gzipped)");
		System.err
				.println("<eTgtCorpus> english target corpus to paraphrase (paraphrases will be extracted that match phrases in this text)");
		System.err.println("<outDir> directory for output files");
		System.err.println("[minTP] (default "
				+ df.format(ParaphraseExtractor.MIN_TRANS_PROB)
				+ ") minimum paraphrase translation probability");
		System.err.println("[minRF] (default "
				+ df.format(ParaphraseExtractor.MIN_REL_FREQ)
				+ ") minimum word relative frequency for common word list");
		System.err.println("[minFinalP] (default "
				+ df.format(ParaphraseExtractor.MIN_FINAL_PROB)
				+ ") minimum final (merged) paraphrase probability");
		System.err.println("[symbols] string of symbols to filter");
	}
}
