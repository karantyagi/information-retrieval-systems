package com.ir.project.indexer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.utils.Utilities;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Indexer {

    private static final int MAX_THREADS = 100;

    public static void main(String args[]) throws IOException {
        String resultDir = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "invertedindex";
        String outFile = resultDir + File.separator + "metadata.json";
        if (!new File(resultDir).isDirectory())
        {
            File dir = new File(resultDir);
            dir.mkdirs();
            System.out.println("Created new directory!\n"+resultDir+"\n");
        }

        DocMetadataAndIndex metadata =  generateIndex("src" + File.separator + "main" + File.separator + "resources" + File.separator + "testcollection" + File.separator + "cleanedcorpus");

        try {
            Files.write(Paths.get(outFile), new ObjectMapper().writeValueAsString(metadata).getBytes());
            System.out.println("Index created!\n" + outFile );

            writeIndex(resultDir,metadata.getIndex());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> stopwords = Utilities.getStopWords();

        String resultDir2 = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "stoppedindex";
        String outFile2 = resultDir2 + File.separator + "metadata.json";
        if (!new File(resultDir2).isDirectory())
        {
            File dir = new File(resultDir2);
            dir.mkdirs();
            System.out.println("Created new directory!\n"+resultDir+"\n");
        }

        DocMetadataAndIndex metadata2 =  generateIndex("src" + File.separator
                + "main" + File.separator + "resources" + File.separator
                + "testcollection" + File.separator + "cleanedcorpus", stopwords);

        try {
            Files.write(Paths.get(outFile2), new ObjectMapper().writeValueAsString(metadata2).getBytes());
            System.out.println("Stopped Index created!\n" + outFile2 );

            writeIndex(resultDir2,metadata2.getIndex());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeIndex(String resultDir, Map<String, List<Posting>> index) throws IOException {
        Map<String, List<Posting>> invertedIndex = index;
        List<String> listOfTerms = new ArrayList<String>(invertedIndex.keySet());
        Collections.sort(listOfTerms);
        FileWriter fw = new FileWriter(resultDir+"" + File.separator + "unigramIndex.txt");
        System.out.println(resultDir+"" + File.separator + "unigramIndex.txt");
        BufferedWriter bw= new BufferedWriter(fw);
        for(String term: listOfTerms){
            bw.append(term+"\n");
        }
        bw.close();
        fw.close();
    }

    public static DocMetadataAndIndex generateIndex(String documentPath) {
        return generateIndex(documentPath, null);
    }

    public static DocMetadataAndIndex generateIndex(String documentPath, List<String> stopList) {

        File cleanedDocsFolder = new File(documentPath);
        DocMetadataAndIndex medatada = null;

        if (cleanedDocsFolder.exists() && null != cleanedDocsFolder.listFiles()) {

            List<Future<Pair<String, Map<String, Integer>>>> futureList =
                    new ArrayList<Future<Pair<String, Map<String, Integer>>>>();

            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

            for (File file : cleanedDocsFolder.listFiles()) {
                FileIndexerThread indexerThread = new FileIndexerThread(file.getAbsolutePath(), stopList);
                Future<Pair<String, Map<String, Integer>>> f = executor.submit(indexerThread);
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
