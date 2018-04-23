// =========================
// ADD RELEVANCE INFORMATION
// =========================

// create getter - setters

package com.ir.project.retrievalmodel.bm25retrieval;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.indexer.Posting;

import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.RetrievedDocument;
import com.ir.project.utils.*;


public class BM25 implements RetrievalModel {
    private double k1;
    private double b;
    private double k2;
    private int totalDocs;
    private double avgLength;

    private Map<String, List<Posting>> invertedIndex;
    private Map<String, Integer> docLengths;
    private Map<String, Integer> relevance;

    public BM25(DocMetadataAndIndex metadataAndIndex,Map<String, Integer> relevance, double k1, double k2, double b)throws IOException {

        this.invertedIndex = metadataAndIndex.getIndex();
        this.relevance = relevance;
        this.docLengths = metadataAndIndex.getDocumentLength();
        //this.k1 = 1.2;
        //this.b = 0.75;
        //this.k2 = 100;
        this.k1 = k1;
        this.k2 = k2;
        this.b = b;
        avgLength = this.avgDocLength();
        totalDocs = docLengths.size();
    }

    ///Method to find the average doc length
    private double avgDocLength(){
        double totalLength = 0.0d;
        for(String doc : this.docLengths.keySet()) {
            totalLength += this.docLengths.get(doc);
        }
        return totalLength/this.docLengths.size();
    }

    public List<RetrievedDocument> search(String query) throws IOException {
        List<RetrievedDocument> retrievedDocs = new ArrayList<>();
        List<String> queryTerms;
        queryTerms = Utilities.getQueryTerms(query);
        //queryTerms .forEach(q->System.out.println("QUERY TERM: "+q));

        // Add all docs to retrievedDocsList
        for (Map.Entry<String, Integer> doc : docLengths.entrySet()) {
            retrievedDocs.add(new RetrievedDocument(doc.getKey()));
        }

        // Calculate Query Likelihood probability(score) for all documents
        // (one document at a time for a given query)

        for(RetrievedDocument rd : retrievedDocs){
            rd.setScore(calculateBM25Score(queryTerms,rd.getDocumentID()));
        }

        // sort the docs in decreasing order of score
        Collections.sort(retrievedDocs, (RetrievedDocument a, RetrievedDocument b) -> Double.compare(b.getScore(), a.getScore()));
        return retrievedDocs;
    }

    private double calculateBM25Score(List<String> queryTerms, String docID) throws IOException {

        double bm25score = 0;
        for(String term : queryTerms){
            bm25score = bm25score + termScore(term,docID,getQueryTermFreq(term, queryTerms));
        }
        return bm25score;
    }

    private double termScore(String term, String docID, double queryTermFreq) throws IOException {
        // No relevance information
        // get relevance information for a query from  index and metadata Relevance Judgement File


        // double totalRelevantDocs;
        // double relevantDocsforQuery;

        double r = 0;
        double R = 0;

        double k1 = 1.2;
        double k2 = 100;
        double b = 0.75;
        double K = getNormalizationFactor(docID);

        double f = getTermDocumentFrequency(term,docID); // term frequency of term in given doc
        double qf = queryTermFreq;
        //System.out.println("TF(fi)   : "+f);
        //System.out.println("% of doc : "+ (f*100)/DocLength.getDocLength(rdoc));
        //System.out.println("QTF(qfi): "+qf);

        double ni = computeDocFreq(term);
        double N = totalDocs;
        //System.out.println("Doc Freq:" +ni);

        double firstN = (r+0.5) / (R- r+0.5);
        double firstD = (ni-r +0.5) / (N-ni-R+r+0.5);

        double secondN = (k1 + 1.0)*f;
        double secondD = K+f;

        double thirdN = (k2+1)*qf;
        double thirdD = k2+qf;

        double score = (Math.log(firstN/firstD))*(secondN/secondD)*(thirdN/thirdD);

        //System.out.println("first N : " + firstN);
        //System.out.println("first D : " + firstD);
        //System.out.println("secondN : " + secondN);
        //System.out.println("secondD : " + secondD);
        //System.out.println("thirdN  : " + thirdN);
        //System.out.println("thirdD  : " + thirdD);
        //System.out.println("Score   : " + score);
        return score;
    }

    private double computeDocFreq(String term) {
        List<Posting> postings = invertedIndex.get(term);
        return postings.size();
    }

    //Method to compute the Normalization factor K that normalizes the tf component by document length
    //K=k1((1−b)+b· dl )
    private double getNormalizationFactor(String docID) throws IOException {
        return k1 * ((1 - b) + b * (docLengths.get(docID)/avgLength));
    }

    private double getTermDocumentFrequency(String term, String docID) {
        if(invertedIndex.get(term)!=null){
            List<Posting> indexList = invertedIndex.get(term);
            double termFrequency = 0;
            for (Posting p  : indexList) {
                if(p.getDocumentId().equals(docID)){
                    return p.getFrequency();
                }
            }
            return termFrequency;
        }
        else{
            System.out.println("TERM not in inverted index - pre processing mis-match : "+term);
            return 0;
        }

    }

    private static int getQueryTermFreq(String queryTerm, List<String> queryTerms) {
        int freq = 0;
        for(String s: queryTerms){
            if(s.equals(queryTerm)){
                freq+=1;
            }
        }
        //System.out.println("QTF(qfi)  : "+freq);
        return freq;
    }



    //Main method to run BM25

    public static void main(String[] args) throws IOException {

        // ====================
        // Test query Search
        // ====================

        String query = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        String indexPath = "E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\temp_index\\metadata.json";
        double k1 = 1.2;
        double b = 0.75;
        double k2 = 100;
        // load previously created inverted index and metadata

        ObjectMapper om = new ObjectMapper();
        try {
            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);

            // Relevance Judgements Information

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter the FULL path for Relevance Judgements file : (e.g. /Usr/cacm.rel.txt or c:\\temp\\cacm.rel.txt)");
            //String filePath = br.readLine();
            String filePath = "E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\resources\\cacm.rel.txt";
            System.out.println(filePath);

            final Path relevanceFilePath = Paths.get(filePath);
            if (!Files.isReadable(relevanceFilePath)) {
                System.out.println("Document directory '" + relevanceFilePath.toAbsolutePath() + "' does not exist or is not readable, please check the path");
                System.exit(1);
            }
            if (!new File(filePath).isFile() && (new File(filePath).isDirectory()) ){
                System.out.println("Relevance file not found at the specified path.");
                System.exit(1);
            }

            Map<String, Integer> relevance = new HashMap<>();

            BM25 test = new BM25(metadataAndIndex,relevance, k1,k2,b);
            Utilities.displayRetrieverdDoc(test.search(query));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);

        }
    }



}
