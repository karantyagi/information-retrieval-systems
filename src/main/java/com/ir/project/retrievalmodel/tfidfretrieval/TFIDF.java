// =========================
// ADD RELEVANCE INFORMATION
// =========================

// create getter - setters

package com.ir.project.retrievalmodel.tfidfretrieval;

import java.util.*;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.indexer.Posting;

import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.RetrievalModelType;
import com.ir.project.retrievalmodel.RetrievedDocument;
import com.ir.project.utils.*;


public class TFIDF implements RetrievalModel {

    private int totalDocs;
    private double avgLength;
    private Map<String, List<Posting>> invertedIndex;
    private Map<String, Integer> docLengths;

    public TFIDF(DocMetadataAndIndex metadataAndIndex)throws IOException {

        invertedIndex = metadataAndIndex.getIndex();
        docLengths = metadataAndIndex.getDocumentLength();
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

    @Override
    public RetrievalModelType getModelType() {
        return RetrievalModelType.TFIDF;
    }

    public List<RetrievedDocument> search(SearchQuery query) throws IOException {
        List<RetrievedDocument> retrievedDocs = new ArrayList<>();
        List<String> queryTerms;
        queryTerms = Utilities.getQueryTerms(query.getQuery());
        //queryTerms .forEach(q->System.out.println("QUERY TERM: "+q));

        // Add all docs to retrievedDocsList
        for (Map.Entry<String, Integer> doc : docLengths.entrySet()) {
            retrievedDocs.add(new RetrievedDocument(doc.getKey()));
        }

        // Calculate Query Likelihood probability(score) for all documents
        // (one document at a time for a given query)

        for(RetrievedDocument rd : retrievedDocs){
            rd.setScore(calculateTFIDFScore(queryTerms,rd.getDocumentID()));
        }

        // sort the docs in decreasing order of score
        Collections.sort(retrievedDocs, (RetrievedDocument a, RetrievedDocument b) -> Double.compare(b.getScore(), a.getScore()));
        return retrievedDocs;
    }

    private double calculateTFIDFScore(List<String> queryTerms, String docID) throws IOException {

        double tfidfScore = 0;
        for(String term : queryTerms){
            tfidfScore = tfidfScore + termScore(term,docID,getQueryTermFreq(term, queryTerms));
        }
        return tfidfScore;
    }

    private double termScore(String term, String docID, double queryTermFreq) throws IOException {


        double tf = getTermDocumentFrequency(term,docID); // term frequency of term in given doc
        //System.out.println("TF(fi)   : "+f);
        //System.out.println("% of doc : "+ (f*100)/DocLength.getDocLength(rdoc));
        //System.out.println("QTF(qfi): "+qf);

        double ni = computeDocFreq(term);
        double N = totalDocs;

        double idf = (Math.log(N/ni));
        double score = tf*idf;

        return score;
    }

    private double computeDocFreq(String term) {
        List<Posting> postings = invertedIndex.get(term);
        return postings.size();
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

        String queryText = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        int queryID =1;
        SearchQuery testQuery = new SearchQuery(queryID,queryText);

        String indexPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "invertedindex" + File.separator + "metadata.json";
        // load previously created inverted index and metadata
        ObjectMapper om = new ObjectMapper();
        try {
            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
            TFIDF test = new TFIDF(metadataAndIndex);
            Utilities.displayRetrieverdDoc(test.search(testQuery));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);

        }
    }



}
