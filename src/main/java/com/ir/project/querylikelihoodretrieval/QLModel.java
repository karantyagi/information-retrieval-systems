package com.ir.project.querylikelihoodretrieval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.customretrieval.indexer.DocMetadataAndIndex;
import com.ir.project.customretrieval.indexer.Posting;

import java.io.*;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.nio.file.Paths;
        import java.util.*;

/**
 * Implements BM25 retrieval model
 * Produces a ranked list of documents(using BM25 model) for a given query or a list of queries
 **/
public class QLModel {

    private static Map<String, List<Posting>> invertedIndex;
    //private static Map<String, List<Posting>> invertedListsForQuery;
    private static Map<String, Integer> docLengths;

    public static void loadIndex(String indexPath){

        // load previously created index

        ObjectMapper om = new ObjectMapper();
        try {
            DocMetadataAndIndex metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
            System.out.println(metadataAndIndex.getIndex().get("Glossary"));
            invertedIndex = null;
            docLengths = null;
            
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        String indexPath = "E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\temp_index\\metadata.json";
        loadIndex(indexPath);
    }

}
