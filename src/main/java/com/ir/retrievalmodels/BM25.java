package com.ir.retrievalmodels;

import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.ir.project.retrievalmodel.RetrievalModel;

public class BM25  implements RetrievalModel {
			public static double k1=1.2;
			public static double b=0.75;
			public static double K;
			public static double k2 = 100;
			public static double totalDocs = 0.0d;
			public static Map<String, Integer> docLenmap = new HashMap<>();
			public static Map<String, String> unigramMap = new HashMap<>();
			public static Map<String, Integer> qF = new HashMap<>();
			public static Map<String, Double> scoreMap = new HashMap<>();
			public static Set<String> sortedScoreSet;

	
	public Set<String> search(String query) {
		return null;
	}
	
		
		/**
		 * 
		 * 
		 * @throws NumberFormatException
		 * @throws IOException
		 */
		//Method to find the inverted index
		public static Double findTotalDocs(String termsPerDocument ) throws NumberFormatException, IOException {
			String line;
			BufferedReader reader = new BufferedReader(new FileReader
					(termsPerDocument));
			while((line = reader.readLine()) != null){
				//Split on "="
				String[] temp = line.split("=");
				//put the results in map
				docLenmap.put(temp[0].replace("[{}]", ""), Integer.parseInt(temp[1]));
				totalDocs = totalDocs + Double.parseDouble(temp[1]);	
			}
			return totalDocs;
		}



		/**
		 * 
		 * @throws IOException
		 *///Method to find the avergae doc length
		public static Double avgDocLength() throws IOException {
			Double avgLen = totalDocs/docLenmap.size();
			return avgLen;
		}
		/**
		 * 
		 * 
		 * @throws IOException
		 */
		//Method to compute the Normalization factor K that normalizes the tf component by document length
		//K=k1((1−b)+b· dl )
		public static Double getNormalizationFactor() throws IOException {
			K = k1 * ((1 - b) + b *avgDocLength());
			return K;
		}


		//Method to find the IDF factor ,r and R are set to zero since there is no relevance information
		public static double getIDF(int ni) {
			return Math.log((((1 + (totalDocs - ni + 0.5) / (ni + 0.5)))));
		}
		/**
		 * 
		 * @param query
		 * @throws IOException
		 *///Method to find the BM25 score 
		public static Map<String, Double> findScore(String query,String indexPath) throws IOException {
			String line1;
			BufferedReader reader = new BufferedReader(new FileReader
					(indexPath));
			for(String q : query.split(" ")) {
				qF.put(q, qF.getOrDefault(q, 0) + 1);
			}
			while((line1 = reader.readLine()) != null){
				String[]keyword=line1.split("#");
				unigramMap.put(keyword[0], keyword[1]);
			}		
			String[] queries = query.split(" ");
			for(String q : queries) {
				int queryFrequency = qF.get(q);
				if(unigramMap.containsKey(q)) {
					String tempDocs = unigramMap.get(q);
					String[] docFreqPair = tempDocs.split("~!~");
					for(String doc : docFreqPair) {
						String[] temp = doc.split("=");
						String title = temp[0];
						Double tf = Double.parseDouble(temp[1]);

						double weightedScore=
								getIDF(docFreqPair.length) *((k1 + 1) * tf / (K + tf))
								*((k2+1d)*queryFrequency/(k2+queryFrequency));
						scoreMap.put(title, scoreMap.getOrDefault(title, 0.0d) + weightedScore);
					}

				}
			}
			return scoreMap;
		}

		/**
		 * 
		 * @param qID
		 * @param query
		 * @throws IOException
		 */
		//Method to sort the documents according to score and find the top 100 results and write them to file
		public static Set<String> topdocs(int qID,String query,String indexPath) throws IOException{
			findScore(query,indexPath);
			sortedScoreSet = new TreeSet<>((a,b) ->{
				if(scoreMap.get(a) <= scoreMap.get(b)) return 1;
				else return -1;
			});
			for(String s : scoreMap.keySet()) {
				sortedScoreSet.add(s);
			}
			int count=0;
			File bm25File=new File(query+"_bm25Scores.txt");
			FileWriter writer1 = new FileWriter(bm25File);
			for(String str: sortedScoreSet) {
				writer1.write(qID+" Q0 "+str.replaceAll(" ", "_"));
				writer1.write(" ");
				writer1.write(count + "");
				writer1.write(" ");
				writer1.write(scoreMap.get(str) + " "+"Bm25NoStopNoStem");
				count++;
				writer1.write("\n");
				if(count>=100)break;
			}
			writer1.close();	
			return sortedScoreSet;

		}

//		public static void main(String[] args) throws IOException {
//			Task2 t=new Task2();
//			//Enter the query
//			System.out.println("Enter the path to termsPerDocument file");
//			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//			String path=reader.readLine();
//			System.out.println("Enter the path to index");
//			String indexPath=reader.readLine();
//			System.out.println("Enter the Query Id");
//			String qID=reader.readLine();
//			System.out.println("Enter the query");
//			String query=reader.readLine();
//			t.findTotalDocs(path);
//			t.topdocs(Integer.parseInt(qID), query, indexPath);
//			System.exit(0);
//		}

	}


