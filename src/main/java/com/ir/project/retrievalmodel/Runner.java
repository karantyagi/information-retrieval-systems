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

    public Runner(String modelRun) {}

        public Runner() {
                ObjectMapper om = new ObjectMapper();
                try {
                    String indexPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "invertedindex" +  File.separator + "metadata.json";
                    this.metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
        }

    public void run(List<SearchQuery> queries) {
        //TODO:

        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator;

        runTFIDFModel(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        System.out.println(" ============================================== TFID Retrieval Run complete ======");
        runBM25Model(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        System.out.println(" ============================================== BM25 Retrieval Run complete ======");
        runQueryLikelihoodModel(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        System.out.println(" ============================================== Smoothed Query Likelihood Retrieval Run complete ======");

    }

    private void runLucene(List<SearchQuery> queries, String SystemRunName,String outputDir) {
        RetrievalModel lucene = new LuceneRetrievalModel();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(lucene, q, outputDir,SystemRunName);
            Future<List<RetrievedDocument>> f = executor.submit(task);
            futures.add(f);
        }

        executor.shutdown();
        pollForCompletion(futures);

    }

    private void runTFIDFModel(List<SearchQuery> queries, String SystemRunName, String outputDir) {
        try {
            RetrievalModel tfidf = new TFIDF(metadataAndIndex);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(tfidf, q, outputDir, SystemRunName);
                Future<List<RetrievedDocument>> f = executor.submit(task);
                futures.add(f);
            }
            executor.shutdown();
            pollForCompletion(futures);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runBM25Model(List<SearchQuery> queries, String SystemRunName, String outputDir) {
        double k1 = 1.2;
        double b = 0.75;
        double k2 = 100;

        RetrievalModel bm25 = null;
        try {
            bm25 = new BM25(this.metadataAndIndex, k1, k2, b);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(bm25, q, outputDir,SystemRunName);
                Future<List<RetrievedDocument>> f = executor.submit(task);
                futures.add(f);
            }
            executor.shutdown();
            pollForCompletion(futures);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runQueryLikelihoodModel(List<SearchQuery> queries, String SystemRunName, String outputDir) {

        double smoothingFactor = 0.35;
        RetrievalModel queryLikelihoodModel = new QLModel(metadataAndIndex, smoothingFactor);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<List<RetrievedDocument>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(queryLikelihoodModel, q, outputDir, SystemRunName);
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

        // TODO add fecth from file logic

        return searchQueryList;
    }

    public static void main(String args[]) throws IOException {

        //TODO: task1 task2 task3

        Runner testRun = new Runner();

        String queryText = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        int queryID =1;
        SearchQuery testQuery = new SearchQuery(queryID,queryText);

        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";

        List<SearchQuery> queries = testRun.fetchSearchQueries(queriesFilePath);
        // ------- comment the below out -------
        queries.add(testQuery);

        // ==============================================================
        // Run 1,2,3: TFIDFNoStopNoStem, BM25NoStopNoStem, QLNoStopNoStem
        // ==============================================================

        testRun.run(queries);

        // ==========================
        // Run 4: LuceneNoStopNoStem
        // ==========================

        Runner testRunLucene = new Runner(RetrievalModelType.LUCENE.name());
        LuceneRetrievalModel runLucene = new LuceneRetrievalModel();
        String luceneIndexDirPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "luceneindex" +  File.separator;
        runLucene.loadIndex(luceneIndexDirPath);
        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator;
        testRunLucene.runLucene(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        System.out.println(" ============================================== Lucene(default settings) Retrieval Run complete ======");

    }
}
