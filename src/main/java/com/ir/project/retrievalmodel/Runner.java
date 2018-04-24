package com.ir.project.retrievalmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.retrievalmodel.bm25retrieval.BM25;
import com.ir.project.retrievalmodel.luceneretrieval.LuceneRetrievalModel;
import com.ir.project.retrievalmodel.querylikelihoodretrieval.QLModel;
import com.ir.project.retrievalmodel.tfidfretrieval.TFIDF;
import com.ir.project.utils.SearchQuery;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Runner {

    private DocMetadataAndIndex metadataAndIndex;

    public Runner(DocMetadataAndIndex docMetadataAndIndex) {
        this.metadataAndIndex = docMetadataAndIndex;
    }

    public Runner() {
        ObjectMapper om = new ObjectMapper();
        try {
            String indexPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "invertedindex" +  File.separator + "metadata.json";
            metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run() {
        //TODO:
        String queriesFilePath = "";
        List<SearchQuery> queries = fetchSearchQueries(queriesFilePath);
        
        runBM25Model(queries);
        runQueryLikelihoodModel(queries);
        runTFIDFModel(queries);
        runLucene(queries);
        
    }

    private void runLucene(List<SearchQuery> queries) {
        RetrievalModel lucene = new LuceneRetrievalModel();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(lucene, q, "src/output/");
            Future<List<RetrievedDocument>> f = executor.submit(task);
            futures.add(f);
        }

        executor.shutdown();
        pollForCompletion(futures);

    }

    private void runTFIDFModel(List<SearchQuery> queries) {
        try {
            RetrievalModel tfidf = new TFIDF(metadataAndIndex);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(tfidf, q, "src/output/");
                Future<List<RetrievedDocument>> f = executor.submit(task);
                futures.add(f);
            }
            executor.shutdown();
            pollForCompletion(futures);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runBM25Model(List<SearchQuery> queries) {
        double k1 = 1.2;
        double b = 0.75;
        double k2 = 100;

        RetrievalModel bm25 = null;
        try {
            bm25 = new BM25(this.metadataAndIndex, k1, k2, b);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(bm25, q, "src/output/");
                Future<List<RetrievedDocument>> f = executor.submit(task);
                futures.add(f);
            }
            executor.shutdown();
            pollForCompletion(futures);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runQueryLikelihoodModel(List<SearchQuery> queries) {

        double smoothingFactor = 0.35;
        RetrievalModel queryLikelihoodModel = new QLModel(metadataAndIndex, smoothingFactor);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(queryLikelihoodModel, q, "src/output/");
            Future<List<RetrievedDocument>> f = executor.submit(task);
            futures.add(f);
        }
        executor.shutdown();
        pollForCompletion(futures);
    }


    private void pollForCompletion(List<Future<List<RetrievedDocument>>> futures) {
        for (Future<List<RetrievedDocument>> f: futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }



    public List<SearchQuery> fetchSearchQueries(@NotNull String queryFilePath) {
        List<SearchQuery> searchQueryList = new ArrayList<>();
        return searchQueryList;
    }

    public static void main(String args[]) {
        //TODO: task1 task2 task3
    }
}
