package com.ir.retrievalmodels;

import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.customretrieval.indexer.DocMetadataAndIndex;
import com.ir.project.customretrieval.indexer.Posting;
import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.SearchedDocument;
import com.ir.utils.Metadata;

public class TFIDF  implements RetrievalModel {
			public static double k1=1.2;
			public static double b=0.75;
			public static double K;
			public static double k2 = 100;
			public static Map<String, Integer> qF = new HashMap<>();
			public static Map<String, Double> scoreMap = new HashMap<>();
			public static Set<String> sortedScoreSet;

		    private static Map<String, List<Posting>> index;
		    private static Map<String, Integer> docLengths;
		    private static int totalDocs = 0;
		    

		    public static void loadIndex(String indexPath){

		        // load previously created index

		        ObjectMapper om = new ObjectMapper();
		        try {
		            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
		            System.out.println(metadataAndIndex.getIndex().get("Glossary"));
		            index = metadataAndIndex.getIndex();
		            docLengths = metadataAndIndex.getDocumentLength();
		            totalDocs = docLengths.size();
		            
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
	
	public Set<SearchedDocument> search(int qID,String query) throws IOException {
		return topdocs(qID, query, index);
	}
	
	
		public static double getIDF(int ni) {
			return Math.log(totalDocs/ni);
		}
		/**
		 * 
		 * @param query
		 * @throws IOException
		 *///Method to find the BM25 score 
		public static Map<String, Double> findScore(String query,Map<String, List<Posting>> index) throws IOException {
			for(String q : query.split(" ")) {
				qF.put(q, qF.getOrDefault(q, 0) + 1);
			}	
			String[] queries = query.split(" ");
			for(String q : queries) {
				if(index.containsKey(q)) {
					List<Posting> postings = index.get(q);
					for(Posting p : postings) {
						String title = p.getDocumentId();
						Double tf = (double)p.getFrequency();

						double weightedScore=
								getIDF(postings.size()) * tf; 
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
		public static Set<SearchedDocument> topdocs(int qID,String query,Map<String, List<Posting>> index) throws IOException{
			findScore(query,index);
			sortedScoreSet = new TreeSet<>((a,b) ->{
				if(scoreMap.get(a) <= scoreMap.get(b)) return 1;
				else return -1;
			});
			for(String s : scoreMap.keySet()) {
				sortedScoreSet.add(s);
			}
			Set<SearchedDocument> res = new TreeSet<>();
			int rank = 1;
			for(String s : sortedScoreSet) {
				SearchedDocument doc = new SearchedDocument(s);
				doc.updateScore(scoreMap.get(s));
				doc.updateRank(rank++);
				res.add(doc);
			}
			return res;
		}

	}


