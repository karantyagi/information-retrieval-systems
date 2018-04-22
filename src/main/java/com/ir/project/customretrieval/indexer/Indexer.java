package com.ir.project.customretrieval.indexer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Indexer {

    private static final int MAX_THREADS = 100;


    public static void main(String []args) {
        Map<String, List<Posting>> index =  generateIndex("/tmp/irproject/");

        try {
            System.out.println(new ObjectMapper().writeValueAsString(index.get("andgit ")));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, List<Posting>> generateIndex(String documentPath) {

        File cleanedDocsFolder = new File(documentPath);
        Map<String, List<Posting>> index = new HashMap<String, List<Posting>>();

        if (cleanedDocsFolder.exists()) {

            List<Future<Pair<String, Map<String, Integer>>>> futureList =
                    new ArrayList<Future<Pair<String, Map<String, Integer>>>>();

            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

            for (File file : cleanedDocsFolder.listFiles()) {
                FileIndexerThread indexerThread = new FileIndexerThread(file.getAbsolutePath());
                Future f = executor.submit(indexerThread);
                futureList.add(f);
            }

            index = pollAndMerge(futureList);

            return index;
        } else {
            System.out.println("Folder " + documentPath + " doesn't exists.");
        }

        return index;
    }

    private static Map<String,List<Posting>> pollAndMerge(List<Future<Pair<String, Map<String, Integer>>>> futureList) {
        Map<String, List<Posting>> index = new HashMap<String, List<Posting>>();

        for (Future<Pair<String, Map<String, Integer>>> f : futureList) {
            try {

                Pair<String, Map<String, Integer>> pair = f.get();


                String docId = pair.getKey();
                Map<String, Integer> wordMap = pair.getValue();
                for(Map.Entry<String, Integer> entry : wordMap.entrySet()) {
                    String term = entry.getKey();
                    Integer termFreqInDoc = entry.getValue();
                    Posting posting = new Posting(docId, termFreqInDoc);
                    if (index.containsKey(term)) {
                        index.get(term).add(posting);
                    } else {
                        List<Posting> postingList = new ArrayList();
                        postingList.add(posting);
                        index.put(term, postingList);
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return index;
    }
}
