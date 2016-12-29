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

public class TagGenerator {
	
	HashMap<String, Double> idf;
	
	public TagGenerator(File idfFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(idfFile));
		HashMap<String, Double> idf = new HashMap<String, Double>();
		
		String line = "";
		while((line = br.readLine()) != null){
			String[] comp = line.split(",");
			idf.put(comp[0], Double.parseDouble(comp[1]));
		}
		
		br.close();
		
		this.idf = idf;
	}
	
	public TagGenerator(HashMap<String, Double> idf){
		this.idf = idf;
	}
	
	public String[] generateTags(String document, int num){
		return generateTags(prepareDoc(document), num);
	}
	
	public String[] generateTags(String[] document, int num){
		TFIDFHolder[] tfidf = tfidf(document);
		
		int to = Math.min(tfidf.length, num);
		String[] result = new String[to];
		for(int i = 0; i < to; i++){
			result[i] = tfidf[i].term;
		}
		
		return result;
	}
	
	public TFIDFHolder[] tfidf(String[] doc){
		HashMap<String, Double> tf = termFrequency(doc);
		List<TFIDFHolder> result = new ArrayList<TFIDFHolder>(tf.size());
		
		Iterator<Entry<String, Double>> it = tf.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Double> pair = (Entry<String, Double>)it.next();
			
			if(pair.getKey() == null){
				continue;
			}
			
			double idfr = Math.log((double)1/(double)22038615);
			if(idf.containsKey(pair.getKey())){
				idfr = idf.get(pair.getKey());
			}
			
			result.add(new TFIDFHolder(pair.getKey(), pair.getValue() * idfr));
		}
		
		result.sort(new TFIDFHolderComparator());
		return result.toArray(new TFIDFHolder[result.size()]);
	}
	
	public static HashMap<String, Double> termFrequency(String[] doc){
		HashMap<String, Double> result = rawTermFrequency(doc);
		Iterator<Entry<String, Double>> it = result.entrySet().iterator();
		
		double highest = 1;
		while(it.hasNext()){
			Map.Entry<String, Double> pair = (Entry<String, Double>)it.next();
			if(pair.getValue() > highest){
				highest = pair.getValue();
			}
		}
		
		it = result.entrySet().iterator();
		
		while(it.hasNext()){
			Map.Entry<String, Double> pair = (Entry<String, Double>)it.next();
			result.put(pair.getKey(), pair.getValue()/highest);
		}
		
		return result;
	}
	
	public static HashMap<String, Double> rawTermFrequency(String[] doc){
		HashMap<String, Double> result = new HashMap<String, Double>();
		
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
	
	public static String[] prepareDoc(String doc){
		return doc.toLowerCase().replaceAll("[^ a-zA-Z]", "").replaceAll(" . ", "").replaceAll(" {2,}", "").split(" ");
	}
	
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
