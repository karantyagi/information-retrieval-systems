package com.ir.project.customretrieval.indexer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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


    public static void main(String args[]) {
        String outFile = "E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\temp_index\\metadata.json";
        DocMetadataAndIndex medatada =  generateIndex("E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\tmp");

        try {
            Files.write(Paths.get(outFile), new ObjectMapper().writeValueAsString(medatada).getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(outFile);
    }

    public static DocMetadataAndIndex generateIndex(String documentPath) {

        File cleanedDocsFolder = new File(documentPath);
        DocMetadataAndIndex medatada = null;

        if (cleanedDocsFolder.exists()) {

            List<Future<Pair<String, Map<String, Integer>>>> futureList =
                    new ArrayList<Future<Pair<String, Map<String, Integer>>>>();

            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

            for (File file : cleanedDocsFolder.listFiles()) {
                FileIndexerThread indexerThread = new FileIndexerThread(file.getAbsolutePath());
                Future f = executor.submit(indexerThread);
                futureList.add(f);
            }
            executor.shutdown();
            medatada = pollAndMerge(futureList);

            return medatada;
        } else {
            System.out.println("Folder " + documentPath + " doesn't exists.");
        }


        return medatada;
    }

    private static DocMetadataAndIndex pollAndMerge(List<Future<Pair<String, Map<String, Integer>>>> futureList) {
        Map<String, List<Posting>> index = new HashMap<String, List<Posting>>();

        Map<String, Integer> documentLengthData = new HashMap<>();
        for (Future<Pair<String, Map<String, Integer>>> f : futureList) {
            try {

                Pair<String, Map<String, Integer>> pair = f.get();

                String docId = pair.getKey();
                int documentLength = 0;

                Map<String, Integer> wordMap = pair.getValue();
                for(Map.Entry<String, Integer> entry : wordMap.entrySet()) {
                    String term = entry.getKey();
                    Integer termFreqInDoc = entry.getValue();
                    documentLength+= termFreqInDoc;
                    Posting posting = new Posting(docId, termFreqInDoc);

                    if (index.containsKey(term)) {
                        index.get(term).add(posting);
                    } else {
                        List<Posting> postingList = new ArrayList();
                        postingList.add(posting);
                        index.put(term, postingList);
                    }

                }

                documentLengthData.put(docId, documentLength);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return new DocMetadataAndIndex(index, documentLengthData);
    }
}
