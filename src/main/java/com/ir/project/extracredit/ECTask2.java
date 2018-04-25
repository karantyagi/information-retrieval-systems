package com.ir.project.extracredit;
import com.ir.project.indexer.Posting;
import com.ir.project.retrievalmodel.Runner;
import com.ir.project.utils.SearchQuery;
import com.sun.javafx.collections.MappingChange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class ECTask2 {

	

	    public static String mitigate(String query, HashMap<String, List<Posting>> index) throws IOException{
	        String[] temp = query.split(" ");
	        for(String word : temp){
	            if(!index.containsKey(word)){
	                String match = word;
	                int minD = Integer.MAX_VALUE;
	                for(String key : index.keySet()){
	                    if(Math.abs(key.length() - word.length()) <= 2){
	                        int distance = minDistance(key, word);
	                        if(distance < minD){
	                            minD = distance;
	                            match = key;
	                        }
	                    }
	                }
	                
	                query = query.replace(word, match);
	        		List<SearchQuery> cleanQuery=new ArrayList<SearchQuery>();
	        		Runner r=new Runner();
	        		List<SearchQuery> l11= r.fetchSearchQueries("/Users/harshmeet/Desktop/IR/errorModel/cacm.query.txt");
	        		for(SearchQuery s: l11) {
	        			 s.setQuery(s.getQuery()); 
	        		}
	        		ECTask2 e=new ECTask2();
	        		for (SearchQuery sQuery : l11) {
	        	           String updatedQuery = e.mitigate(sQuery.getQuery(),index );
	        	           sQuery.setQuery(updatedQuery);
	        	    }

	            }
	        }
	        return l11;
	    }
	    public static int minDistance(String word1, String word2) {
	        int[][] c = new int[word1.length()+1][word2.length()+1];
	        char[] a = word1.toCharArray();
	        char[] b = word2.toCharArray();
	        for(int i = 0; i<c.length ; i++){
	            for(int j=0; j<c[0].length; j++){
	                if(i==0) {
	                    c[i][j] = j;

	                    continue;
	                };
	                if(j==0) {
	                    c[i][j] = i;

	                    continue;
	                };
	                int same=0;
	                if(a[i-1]==b[j-1]) {
	                    same= c[i-1][j-1];

	                }
	                else same = 1+ c[i-1][j-1];
	                //insert
	                int insert = 1+c[i][j-1];
	                //delete
	                int delete =1+ c[i-1][j];
	               


	                c[i][j] = Math.min(insert,Math.min(same,delete));
	            }
	        }
	        return c[word1.length()][word2.length()];
	    }
	}

