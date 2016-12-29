package com.arctro.taggenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("example.txt"));
		String text = "";
		String line = "";
		while((line = br.readLine()) != null){
			text+=line;
		}
		
		br.close();
		
		TagGenerator tg = new TagGenerator(new File("idf.csv"));
		System.out.println(Arrays.toString(tg.generateTags(TagGenerator.prepareDoc(text), 10)));
	}

}
