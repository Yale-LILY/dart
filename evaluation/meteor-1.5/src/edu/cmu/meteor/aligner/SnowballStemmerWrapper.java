package edu.cmu.meteor.aligner;

import org.tartarus.snowball.SnowballStemmer;

/**
 * 
 * SNOWBALL STEMMERS ARE NOT THREADSAFE. THIS IS WORKED AROUND WITH A
 * SYNCHRONIZED METHOD. WHILE TECHNICALLY THREADSAFE, THIS WILL SLOW DOWN
 * MULTITHREADED APPLICATIONS. USE THE COPY CONSTRUCTORS FOR Aligner AND
 * MeteorScorer FOR MULTITHREADED APPLICATIONS.
 * 
 */
public class SnowballStemmerWrapper implements Stemmer {

	private SnowballStemmer stemmer;

	public SnowballStemmerWrapper(SnowballStemmer stemmer) {
		this.stemmer = stemmer;
	}

	/**
	 * 
	 * THIS METHOD IS A SYNCHRONIZED WRAPPER FOR NON-THREADSAFE SNOWBALL
	 * STEMMERS.
	 */
	public synchronized String stem(String word) {
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}
