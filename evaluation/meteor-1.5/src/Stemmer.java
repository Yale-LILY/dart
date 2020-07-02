import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import edu.cmu.meteor.util.Constants;

public class Stemmer {
	public static void main(String[] args) throws Throwable {
		if (args.length != 1) {
			System.err
					.println("Snowball stem some text in a supported language");
			System.err
					.println("Languages: en ar da de es fi fr hu it nl no pt ro ru se tr");
			System.err.println("Usage: Stemmer lang < in > out");
			System.exit(1);
		}

		String lang = Constants.normLanguageName(args[0]);
		edu.cmu.meteor.aligner.Stemmer stemmer = Constants.newStemmer(lang);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;

		while ((line = in.readLine()) != null) {
			StringBuilder sb = new StringBuilder();
			StringTokenizer tok = new StringTokenizer(line);
			String word;
			while (tok.hasMoreTokens()) {
				word = tok.nextToken();
				if (!isNumber(word)) {
					word = stemmer.stem(word);
				}
				sb.append(" " + word);
			}
			System.out.println(sb.toString().trim());
		}
	}

	private static boolean isNumber(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!(Character.isDigit(s.charAt(i)) || s.charAt(i) == ','
					|| s.charAt(i) == '.' || s.charAt(i) == '-')) {
				return false;
			}
		}
		return true;
	}
}
