package com.arctro.taggenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Calculates tags for an inputed document using TF-IDF
 * @author Ben McLean
 */
public class TagGenerator {
	
	HashMap<String, Double> idf;
	private static final double DEFAULT_IDF = Math.log((double)1/(double)1000000000);
	boolean allowUnknown = true;
	
	/**
	 * Initialize a TagGenerator with a file of idfs
	 * @param idfFile List of terms and inverse document frequencies, using the algorithm log(document occurances, total documents)
	 * @throws IOException Failed to read provided file
	 */
	public TagGenerator(File idfFile) throws IOException{
		//Init reader
		BufferedReader br = new BufferedReader(new FileReader(idfFile));
		//Init index
		HashMap<String, Double> idf = new HashMap<String, Double>();
		
		//Read into index
		String line = "";
		while((line = br.readLine()) != null){
			String[] comp = line.split(",");
			idf.put(comp[0], Double.parseDouble(comp[1]));
		}
		
		br.close();
		
		this.idf = idf;
	}
	
	/**
	 * Initialize a TagGenerator with an index of inverse document frequencies
	 * @param idf Index of inverse document frequencies, using the algorithm log(document occurances, total documents)
	 */
	public TagGenerator(HashMap<String, Double> idf){
		this.idf = idf;
	}
	
	/**
	 * Generates the tags for a document
	 * @param document A document
	 * @param num Maximum number of tags to return
	 * @return The tags for a document
	 */
	public String[] generateTags(String document, int num){
		return generateTags(prepareDoc(document), num);
	}
	
	/**
	 * Generates the tags for a document
	 * @param document A prepared document
	 * @param num Maximum number of tags to return
	 * @return The tags for a document
	 */
	public String[] generateTags(String[] document, int num){
		TFIDFHolder[] tfidf = tfidf(document);
		
		int to = Math.min(tfidf.length, num);
		String[] result = new String[to];
		for(int i = 0; i < to; i++){
			result[i] = tfidf[i].term;
		}
		
		return result;
	}
	
	/**
	 * Calculates the Term Frequency-Inverse Document Frequency of a document, and returns an order list of each term
	 * @param doc A prepared document
	 * @return The Term Frequency-Inverse Document Frequency of a document, as an ordered list of each term
	 */
	public TFIDFHolder[] tfidf(String[] doc){
		//Get the wieghted term frequencies of the document
		HashMap<String, Double> tf = termFrequency(doc);
		//Initialize the result array to the length of tf
		List<TFIDFHolder> result = new ArrayList<TFIDFHolder>(tf.size());
		
		//Iterate through the term frequencies map
		Iterator<Entry<String, Double>> it = tf.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Double> pair = (Entry<String, Double>)it.next();
			
			//Skip if the key is null
			if(pair.getKey() == null){
				continue;
			}
			
			//If the term is not listed in the ifd index, the default value is calculated
			//as log( 1 / total documents)
			double idfr = DEFAULT_IDF;
			if(idf.containsKey(pair.getKey())){
				idfr = idf.get(pair.getKey());
			}else if(!allowUnknown){
				continue;
			}
			
			//Add TF-IDF to results
			result.add(new TFIDFHolder(pair.getKey(), pair.getValue() * idfr));
		}
		
		//Sort results
		result.sort(new TFIDFHolderComparator());
		return result.toArray(new TFIDFHolder[result.size()]);
	}
	
	/**
	 * Returns the weighted term frequency for each term
	 * @param doc A prepared document
	 * @return The weighted term frequency for each term
	 */
	public static HashMap<String, Double> termFrequency(String[] doc){
		//Get the raw term frequencies
		HashMap<String, Double> result = rawTermFrequency(doc);
		//Get iterator
		Iterator<Entry<String, Double>> it = result.entrySet().iterator();
		
		//Calculate the highest term frequency
		double highest = 1;
		while(it.hasNext()){
			Map.Entry<String, Double> pair = (Entry<String, Double>)it.next();
			if(pair.getValue() > highest){
				highest = pair.getValue();
			}
		}
		
		it = result.entrySet().iterator();
		
		//Divide every term frequency by the highest
		while(it.hasNext()){
			Map.Entry<String, Double> pair = (Entry<String, Double>)it.next();
			result.put(pair.getKey(), pair.getValue()/highest);
		}
		
		return result;
	}
	
	/**
	 * Generates the raw term frequency for a document
	 * @param doc A prepared document
	 * @return A HashMap of each term and its raw term frequency
	 */
	public static HashMap<String, Double> rawTermFrequency(String[] doc){
		//Init HashMap
		HashMap<String, Double> result = new HashMap<String, Double>();
		
		//Loop through doc[], counting each occurrence of each word
		for(int i = 0; i < doc.length; i++){
			Double tf = result.get(doc[i]);
			if(tf == null){
				result.put(doc[i], (double)1);
			}else{
				result.put(doc[i], tf+1);
			}
		}
		
		return result;
	}
	
	/**
	 * Prepares a document for analysis
	 * @param doc The document to prepare
	 * @return The document ready for analysis
	 */
	public static String[] prepareDoc(String doc){
		//Remove all non alpha characters, all double spaces, and any single characters, then split by spaces
		return doc.toLowerCase().replaceAll("[^ a-zA-Z]", "").replaceAll(" . ", "").replaceAll(" {2,}", "").split(" ");
	}
	
	/**
	 * Sets if unknown words are allowed to be used. Disabling this prevents typos from being included.
	 * @param allowUnknown If unknown words are allowed to be used
	 */
	public void setAllowUnknown(boolean allowUnknown){
		this.allowUnknown = allowUnknown;
	}
	
	/**
	 * Orders a TFIDF Holder array
	 * @author Ben McLean
	 */
	public static class TFIDFHolderComparator implements Comparator<TFIDFHolder>{
		@Override
		public int compare(TFIDFHolder o1, TFIDFHolder o2) {
			if(o1.tfidf == o2.tfidf){
				return 0;
			}else if(o1.tfidf > o2.tfidf){
				return 1;
			}else{
				return -1;
			}
		}
	}
}
