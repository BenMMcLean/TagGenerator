package com.arctro.taggenerator;

public class TFIDFHolder {
	String term;
	double tfidf;
	
	public TFIDFHolder(String term, double tfidf) {
		super();
		this.term = term;
		this.tfidf = tfidf;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public double getTfidf() {
		return tfidf;
	}

	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}
	
	@Override
	public String toString() {
		return "TFIDFHolder [term=" + term + ", tfidf=" + tfidf + "]";
	}
}
