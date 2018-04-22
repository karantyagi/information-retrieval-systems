package com.ir.project.querylikelihoodretrieval;

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

    public QLModel(){

      invertedIndex = null;
      docLengths = null;

    }

}
