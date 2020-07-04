package edu.cmu.parex;

public class Paraphrase {
	int[] ref;
	int[] par;
	double prob;

	public Paraphrase(int[] ref, int[] par, double prob) {
		this.ref = ref;
		this.par = par;
		this.prob = prob;
	}
}
