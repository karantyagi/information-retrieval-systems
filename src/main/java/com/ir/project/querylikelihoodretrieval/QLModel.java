package com.ir.project.querylikelihoodretrieval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.customretrieval.indexer.DocMetadataAndIndex;
import com.ir.project.customretrieval.indexer.Posting;
import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.RetrievedDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implements BM25 retrieval model
 * Produces a ranked list of documents(using BM25 model) for a given query or a list of queries
 **/
public class QLModel implements RetrievalModel {

    private static Map<String, List<Posting>> invertedIndex;
    //private static Map<String, List<Posting>> invertedListsForQuery;
    private static Map<String, Integer> docLengths;

    // =======================================================================
    // This function needs to run once and is common to the 3 retrieval model
    // =======================================================================

    public static void loadIndex(String indexPath) {

        // load previously created inverted index and metadata

        ObjectMapper om = new ObjectMapper();
        try {
            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
            invertedIndex = metadataAndIndex.getIndex();
            docLengths = metadataAndIndex.getDocumentLength();
            System.out.println("\nIndex loaded to memory.\n");
            // System.out.println(metadataAndIndex.getIndex().get("Glossary"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param query
     * @return List of retrieved documents after running Lucene retrieval model on a given query
     */
    public Set<RetrievedDocument> search(String query) throws IOException {

        Set<RetrievedDocument> retrievedDocs = new HashSet<>();

        List<String> queryTerms = new ArrayList<>();
        queryTerms = getQueryTerms(query);

        // Calculate Query Likelihood probability(score) for all documents

        // Add all docs to retrievedDocsList

        List<RetrievedDocument> retrievedDocsList = new ArrayList<>();
        for (Map.Entry<String, Integer> doc : docLengths.entrySet()) {
            retrievedDocsList.add(new RetrievedDocument(doc.getKey()));
        }

        // Calculate score one document at a time for a given document


        for(RetrievedDocument s: retrievedDocsList){
                s.setScore(calculateQueryLikelihoodProbability(queryTerms,s.getDocumentID()));
            }

        // sort the docs and assign ranks

        // return all (not just top 100)

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

        double languageModelProbability;
        int collectionTermFrequency;
        int documentTermFrequency;
        int documentLength = docLengths.get(docID);

        // Jelinek-Mercer Smoothing
        // Smoothing factor = 0.35

        double smoothingFactor = 0.35;
        languageModelProbability = (1-smoothingFactor);
        return languageModelProbability;
    }


    // =========================================
    //  Utility function - add to Utils class
    // =========================================

    public static List<String> getQueryTerms(String query) {

        // ------------------------------------------------------
        //  Add LOGIC for Pre-processing, improve pre-processing
        // ------------------------------------------------------

        List<String> queryTerms = new ArrayList<>();
        String terms[];
        query = query.trim();

        // split on any whitespace
        // Using java's predefined character classes

        final String WHITESPACE = "\\s"; // any whitespace character -  [ \t\n\x0B\f\r]+
        final String MULTIPLE_WHITESPACES = "//s+"; // ????????????? mutliple whitespaces - regex


        terms = query.split(WHITESPACE );
        for(String t : terms) {
            // System.out.println("TERM: "+t);
            // add regex for 1 or more spaces e.g. " " or "    "
            if(!(t.trim().equals("")) && !(t.trim().equals(MULTIPLE_WHITESPACES))){
                queryTerms.add(t.trim());
            }
           // else{
           //     System.out.println("------ Space -------");
           // }

        }
        return queryTerms;
    }


    // =========================================
    //  Main function to TEST
    // =========================================

    public static void main(String[] args) {
        String indexPath = "E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\temp_index\\metadata.json";
        loadIndex(indexPath);

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



    }

    public static void displayIndex(){
        invertedIndex.forEach((k,v)->System.out.println("TERM: "+k+"\n"+v.toString()));
    }

    public static void displayDocLengths(){
        System.out.println("No. of Docs: "+docLengths.size()+"\n");
        docLengths.forEach((k,v)->System.out.println(k+"  |  "+v));
    }


}
