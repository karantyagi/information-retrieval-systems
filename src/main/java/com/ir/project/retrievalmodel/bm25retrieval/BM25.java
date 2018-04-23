package com.ir.project.retrievalmodel.bm25retrieval;


import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.indexer.Posting;

import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.RetrievedDocument;

public class BM25 implements RetrievalModel {
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
    private static double avgLength = 0.0d;

    ///Method to find the avergae doc length
    public static double avgDocLength() throws IOException {
        double totalLength = 0.0d;
        for(String doc : docLengths.keySet()) {
            totalLength += docLengths.get(doc);
        }
        return totalLength/docLengths.size();
    }

    public static void loadIndex(String indexPath){

        // load previously created index

        ObjectMapper om = new ObjectMapper();
        try {
            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
            System.out.println(metadataAndIndex.getIndex().get("Glossary"));
            index = metadataAndIndex.getIndex();
            docLengths = metadataAndIndex.getDocumentLength();
            avgLength = avgDocLength();
            totalDocs = docLengths.size();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<RetrievedDocument> search(String query) throws IOException {
        return null;
    }


    /**
     *
     *
     * @throws IOException
     */
    //Method to compute the Normalization factor K that normalizes the tf component by document length
    //K=k1((1−b)+b· dl )
    public static Double getNormalizationFactor() throws IOException {
        K = k1 * ((1 - b) + b *avgLength);
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
    public static Map<String, Double> findScore(String query,Map<String, List<Posting>> index) throws IOException {
        for(String q : query.split(" ")) {
            qF.put(q, qF.getOrDefault(q, 0) + 1);
        }
        String[] queries = query.split(" ");
        for(String q : queries) {
            int queryFrequency = qF.get(q);
            if(index.containsKey(q)) {
                List<Posting> postings = index.get(q);
                for(Posting p : postings) {
                    String title = p.getDocumentId();
                    Double tf = (double)p.getFrequency();

                    double weightedScore=
                            getIDF(postings.size()) *((k1 + 1) * tf / (K + tf))
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
    public static Set<RetrievedDocument> topdocs(int qID,String query,Map<String, List<Posting>> index) throws IOException{
        findScore(query,index);
        Set<RetrievedDocument> res = new TreeSet<>((a,b) ->{
            if(a.getScore() <= b.getScore()) return 1;
            else return -1;
        });
        int rank = 1;
        for(String s : scoreMap.keySet()) {
            RetrievedDocument doc = new RetrievedDocument(s);
            doc.setScore(scoreMap.get(s));
            res.add(doc);
        }
        for(RetrievedDocument s : res) {
            s.setRank(rank++);
        }
        return res;

    }

    //Main method to run BM25

    public static void main(String[] args) throws IOException {

        BM25 b=new BM25();
        b.loadIndex("E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\lucene-index");
        //System.out.println(b.search(1,  "Algorithms or statistical packages for ANOVA, regression using least squares or generalized linear models. System design, capabilities,statistical formula are of interest.  Student's t test, Wilcoxon and sign tests, multivariate and univariate components can be included"));
    }



}
