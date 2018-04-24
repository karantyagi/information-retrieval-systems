package com.ir.project.retrievalmodel.querylikelihoodretrieval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.indexer.Posting;
import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.RetrievedDocument;
import com.ir.project.utils.*;
import java.io.*;
import java.util.*;

import static com.ir.project.utils.Utilities.*;
/**
 * Implements Smoothed Query Likelihood retrieval model
 * Produces a ranked list of documents(using BM25 model) for a given query or a list of queries
 **/
public class QLModel implements RetrievalModel {

    private Map<String, List<Posting>> invertedIndex;
    private Map<String, Integer> docLengths;
    private int corpusSize; // corpus size is total number of words in collections
    private double smoothingFactor;

    public Map<String, List<Posting>> getInvertedIndex() {
        return invertedIndex;
    }

    public int getCorpusSize() {
        return corpusSize;
    }

    public void setInvertedIndex(Map<String, List<Posting>> invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public Map<String, Integer> getDocLengths() {
        return docLengths;
    }

    public void setDocLengths(Map<String, Integer> docLengths) {
        this.docLengths = docLengths;
    }

    public void setCorpusSize(int corpusSize) {
        this.corpusSize = corpusSize;
    }

    public double getSmoothingFactor() {
        return smoothingFactor;
    }

    public void setSmoothingFactor(double smoothingFactor) {
        this.smoothingFactor = smoothingFactor;
    }

    public QLModel(DocMetadataAndIndex metadataAndIndex, double smoothingFactor) {

            invertedIndex = metadataAndIndex.getIndex();
            docLengths = metadataAndIndex.getDocumentLength();
            // smoothingFactor = 0.35;
            this.smoothingFactor = smoothingFactor;
            System.out.println("\nIndex loaded to memory.\n");

            for (Map.Entry<String, Integer> doc : docLengths.entrySet()) {
                corpusSize += doc.getValue();
            }
            System.out.println("\nCorpus size (total no. of word occurrences in corpus) : "+corpusSize+"\n");
            // System.out.println(metadataAndIndex.getIndex().get("Glossary"));
    }

    /**
     * @param query
     * @return Sorted List of retrieved documents after running the retrieval model for a given query
     */
    public List<RetrievedDocument> search(SearchQuery query) throws IOException {

        List<RetrievedDocument> retrievedDocs = new ArrayList<>();
        List<String> queryTerms;
        queryTerms = getQueryTerms(query.getQuery());
        //queryTerms .forEach(q->System.out.println("QUERY TERM: "+q));

        // Add all docs to retrievedDocsList
        for (Map.Entry<String, Integer> doc : docLengths.entrySet()) {
            retrievedDocs.add(new RetrievedDocument(doc.getKey()));
        }

        // Calculate Query Likelihood probability(score) for all documents
        // (one document at a time for a given query)

        for(RetrievedDocument rd : retrievedDocs){
            rd.setScore(calculateQueryLikelihoodProbability(queryTerms,rd.getDocumentID()));
            }

        // sort the docs in decreasing order of score
        Collections.sort(retrievedDocs, (RetrievedDocument a, RetrievedDocument b) -> Double.compare(b.getScore(), a.getScore()));
        return retrievedDocs;
    }

    private double calculateQueryLikelihoodProbability(List<String> queryTerms, String docID) {

        double score = 1;
        for(String term : queryTerms){
            score = score * termProbability(term,docID);
        }
        return score;
    }

    private double termProbability(String term, String docID) {

        double collectionTermFrequency = getTermCorpusFrequency(term);
        //System.out.println("TERM : "+term);
        //System.out.println("collectionTermFrequency : "+collectionTermFrequency);
        double documentTermFrequency = getTermDocumentFrequency(term,docID);
        //System.out.println("documentTermFrequency : "+documentTermFrequency);
        int documentLength = docLengths.get(docID);
        //System.out.println("doc length : "+documentLength);

        // Jelinek-Mercer Smoothing
        // Smoothing factor = 0.35


        double first = (1-smoothingFactor)*documentTermFrequency/documentLength;
        //System.out.print("First : "+first);
        double second = (smoothingFactor*(collectionTermFrequency/getCorpusSize()));
        //System.out.println("Second : "+second);
        double languageModelProbability = first + second;
        //System.out.println("LM probability: "+languageModelProbability);
        return languageModelProbability;
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

    private double getTermCorpusFrequency(String term) {

        if(invertedIndex.get(term)!=null) {

            List<Posting> indexList = invertedIndex.get(term);
            double corpusFrequency = 0;
            for (Posting p : indexList) {
                corpusFrequency += p.getFrequency();
            }
            return corpusFrequency;
        }
        else{
            System.out.println("TERM not in corpus : "+term);
            return 0;
        }
    }

    public void displayIndex(){
        this.invertedIndex.forEach((k,v)->System.out.println("TERM: "+k+"\n"+v.toString()));
    }

    public void displayDocLengths(){
        System.out.println("No. of Docs: " + this.docLengths.size()+"\n");
        this.docLengths.forEach((k,v)->System.out.println(k+"  |  "+v));
    }

    // =========================================
    //  Main function to TEST
    // =========================================

    public static void main(String[] args) throws IOException {

        //loadIndex(indexPath);

        // ==============================================
        // Print Index and DocLengths after loading them
        // ==============================================

        //displayIndex();
        //displayDocLengths();

        // ====================
        // Test getQueryTerms()
        // ====================

        /*
        String testQuery1 = " What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";

        String testQuery2 = " I'm interested in mechanisms for communicating between disjoint processes,\n" +
                "possibly, but not exclusively, in a distributed environment.  I would\n" +
                "rather see descriptions of complete mechanisms, with or without implementations,\n" +
                "as opposed to theoretical work on the abstract problem.  Remote procedure\n" +
                "calls and message-passing are examples of my interests.";

        //List<String> qTerms1 = getQueryTerms(testQuery1);
        //qTerms1.forEach(query->System.out.println(query));
        //System.out.println();

       List<String> qTerms2 = getQueryTerms(testQuery2);
       qTerms2.forEach(query->System.out.println("QUERY TERM: "+query));

       */

        // ====================
        // Test query Search
        // ====================

        String queryText = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        int queryID =1;
        SearchQuery testQuery = new SearchQuery(queryID,queryText);
        String indexPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "invertedindex" +  File.separator + "metadata.json";
        double smoothingFactor = 0.35;
        // load previously created inverted index and metadata

        ObjectMapper om = new ObjectMapper();
        try {
            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
            QLModel test = new QLModel(metadataAndIndex,smoothingFactor);
            Utilities.displayRetrieverdDoc(test.search(testQuery));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);

        }
    }

}
