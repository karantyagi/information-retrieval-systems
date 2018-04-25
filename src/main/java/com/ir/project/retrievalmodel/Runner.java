package com.ir.project.retrievalmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.retrievalmodel.bm25retrieval.BM25;
import com.ir.project.retrievalmodel.luceneretrieval.LuceneRetrievalModel;
import com.ir.project.retrievalmodel.querylikelihoodretrieval.QLModel;
import com.ir.project.retrievalmodel.tfidfretrieval.TFIDF;
import com.ir.project.evaluation.EvaluationStats;
import com.ir.project.evaluation.Evaluator;
import javafx.util.Pair;
import java.util.*;
import com.ir.project.stemmer.QueryEnhancer;
import com.ir.project.stemmer.StemClassGenerator;
import com.ir.project.utils.SearchQuery;
import com.ir.project.utils.Utilities;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public void run(List<SearchQuery> queries, Map<Integer, List<String>> relevantQueryDocMap) {
        //TODO:
        long start;
        long elapsed;
        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator;
        String snippetDir = "src" + File.separator + "main" + File.separator + "snippets" + File.separator;

        start = System.currentTimeMillis();
        runTFIDFModel(queries,RetrievalModelRun.NoStopNoStem.name(),outFile,snippetDir, relevantQueryDocMap);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n --------------------------------- TFID Retrieval Run complete ------------------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

        start = System.currentTimeMillis();
        runBM25Model(queries,RetrievalModelRun.NoStopNoStem.name(),outFile,snippetDir, relevantQueryDocMap);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n --------------------------------- BM25 Retrieval Run complete ------------------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

        start = System.currentTimeMillis();
        runQueryLikelihoodModel(queries,RetrievalModelRun.NoStopNoStem.name(),outFile,snippetDir, relevantQueryDocMap);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n ------------------------ Smoothed Query Likelihood Retrieval Run complete ------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

    }

    private void runLucene(List<SearchQuery> queries, String systemRunName,String outputDir,String snippetDir,Map<Integer, List<String>> relevantQueryDocMap) {
        RetrievalModel lucene = new LuceneRetrievalModel();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(lucene, q, outputDir,snippetDir, systemRunName);
            Future<Pair<SearchQuery, List<RetrievedDocument>>> f = executor.submit(task);
            futures.add(f);
        }

        executor.shutdown();
        Map<SearchQuery, List<RetrievedDocument>> queriesAndDocs = pollForCompletion(futures);
        evaluateAndPrintStats(relevantQueryDocMap, queriesAndDocs, RetrievalModelType.LUCENE, systemRunName);

    }

    private void runTFIDFModel(List<SearchQuery> queries, String systemRunName, String outputDir, String snippetDir,Map<Integer, List<String>> relevantQueryDocMap) {
        try {
            RetrievalModel tfidf = new TFIDF(metadataAndIndex);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(tfidf, q, outputDir,snippetDir,systemRunName);
                Future<Pair<SearchQuery, List<RetrievedDocument>>> f = executor.submit(task);
                futures.add(f);
            }
            executor.shutdown();
            Map<SearchQuery, List<RetrievedDocument>> queriesAndDocs = pollForCompletion(futures);

            evaluateAndPrintStats(relevantQueryDocMap, queriesAndDocs, RetrievalModelType.TFIDF, systemRunName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runBM25Model(List<SearchQuery> queries, String systemRunName, String outputDir, String snippetDir,Map<Integer, List<String>> relevantQueryDocMap) {
        double k1 = 1.2;
        double b = 0.75;
        double k2 = 100;

        RetrievalModel bm25 = null;
        try {
            bm25 = new BM25(this.metadataAndIndex, k1, k2, b);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(bm25, q, outputDir,snippetDir,systemRunName);
                Future<Pair<SearchQuery, List<RetrievedDocument>>> f = executor.submit(task);
                futures.add(f);
            }
            executor.shutdown();
            Map<SearchQuery, List<RetrievedDocument>> queriesAndDocs = pollForCompletion(futures);

            evaluateAndPrintStats(relevantQueryDocMap, queriesAndDocs, RetrievalModelType.BM25,systemRunName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runQueryLikelihoodModel(List<SearchQuery> queries, String systemRunName, String outputDir, String snippetDir,Map<Integer, List<String>> relevantQueryDocMap) {

        try
        {
        double smoothingFactor = 0.35;
        RetrievalModel queryLikelihoodModel = new QLModel(metadataAndIndex, smoothingFactor);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(queryLikelihoodModel, q, outputDir,snippetDir, systemRunName);
            Future<Pair<SearchQuery, List<RetrievedDocument>>> f = executor.submit(task);
            futures.add(f);
        }
        executor.shutdown();
        Map<SearchQuery, List<RetrievedDocument>> queriesAndDocs = pollForCompletion(futures);

        evaluateAndPrintStats(relevantQueryDocMap, queriesAndDocs, RetrievalModelType.QL,systemRunName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Map<SearchQuery, List<RetrievedDocument>> pollForCompletion(List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures) {
        Map<SearchQuery, List<RetrievedDocument>> queryAndRetreivedDocs = new HashMap<>();
        for (Future<Pair<SearchQuery, List<RetrievedDocument>>> f: futures) {
            try {
                Pair<SearchQuery, List<RetrievedDocument>> pair = f.get();
                queryAndRetreivedDocs.put(pair.getKey(), pair.getValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return queryAndRetreivedDocs;
    }

    private void evaluateAndPrintStats(Map<Integer, List<String>> relevantQueryDocMap, Map<SearchQuery,
            List<RetrievedDocument>> queriesAndDocs, RetrievalModelType bm252, String systemName) {
        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "evaluation" + File.separator;
        Evaluator evaluator = new Evaluator(bm252.name(), relevantQueryDocMap, queriesAndDocs);
        EvaluationStats evaluationStats = evaluator.evaluate();
        evaluationStats.writePrecisionTablesToFolder(relevantDocFilePath);
        System.out.println("--------------------------------------------------");
        System.out.println("STATS (" + evaluationStats.getRunModel()+"_"+systemName + ")");
        System.out.println("MAP: " + evaluationStats.getMap() + "\nMRR: " + evaluationStats.getMrr());
    }


    public List<SearchQuery> fetchSearchQueries(@NotNull String queryFilePath) throws IOException {
        List<SearchQuery> searchQueryList = new ArrayList<>();

        FileInputStream fstream;
        try {

            fstream = new FileInputStream(queryFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String line;
            String fullQuery = "";
            int qID = 0;
            String words[];

            //Read File Line By Line
            while ((line = br.readLine())!= null)   {
                //System.out.println("Line: "+line);
                words = line.trim().split(" ");
                if(words.length==0){
                    //System.out.println("No words. -------- ");
                }
                else {
                    //System.out.println("Words: "+words.length+" | "+words[0]);
                    if(words[0].equals("<DOCNO>")){
                        qID = Integer.parseInt(words[1]);
                        fullQuery = "";
                    }

                    if(words[0].equals("</DOC>")){
                      // System.out.println("qID: "+qID+" Query: "+fullQuery.trim());
                       searchQueryList.add(new SearchQuery(qID,fullQuery.trim()));
                        //System.out.println(" ------------------- QUERY_ID: "+temp.getQueryID()+" added.");
                        //fullQuery = "";
                    }

                    if(!words[0].equals("<DOC>") && !words[0].equals("<DOCNO>") && !words[0].equals("</DOC>")){
                        fullQuery+= line + "\n";
                    }

                }

            }
            //Close the input stream
            br.close();
            fstream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*
        for(SearchQuery s : searchQueryList){
            System.out.println("QUERY: "+s.getQueryID()+"\n"+s.getQuery());
        }
        */


        return searchQueryList;
    }

    public static void main(String args[]) throws IOException {

        System.out.println();

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);


        Runner testRun = new Runner();

        String queryText = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        int queryID =1;
        SearchQuery testQuery = new SearchQuery(queryID,queryText);

        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";

        List<SearchQuery> queries = testRun.fetchSearchQueries(queriesFilePath);
        //List<SearchQuery> queries = new ArrayList<>();
        // ------- comment the below out -------
        ////queries.add(testQuery);

        // ==============================================================
        // Run 1,2,3: TFIDFNoStopNoStem, BM25NoStopNoStem, QLNoStopNoStem
        // ==============================================================

       //// testRun.run(queries,relevantQueryDocMap);



        // ==========================
        // Run 4: LuceneNoStopNoStem
        // ==========================


        Runner testRunLucene = new Runner(RetrievalModelType.LUCENE.name());
        LuceneRetrievalModel runLucene = new LuceneRetrievalModel();
        String luceneIndexDirPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "luceneindex" +  File.separator;
        runLucene.loadIndex(luceneIndexDirPath);
        String outFile =    "src" + File.separator + "main" + File.separator + "output" + File.separator;
        String snippetDir = "src" + File.separator + "main" + File.separator + "snippets" + File.separator;

        long start = System.currentTimeMillis();
        testRunLucene.runLucene(queries,RetrievalModelRun.NoStopNoStem.name(),outFile,snippetDir, relevantQueryDocMap);
        runLucene .closeIndex();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("\n ------------------------ Lucene(default settings) Retrieval Run complete -------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds");



        // ==========================
        // Run 1: TASK 2
        // ==========================

        /*
        String cleanedCorpusDocPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                "testcollection" + File.separator + "cleanedcorpus";

        String tempstemmedCorpusFilePath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                "testcollection" + File.separator + "cacm_stem.txt";

        Map<String, Set<String>> stemClasses =
                new StemClassGenerator(cleanedCorpusDocPath).stemCorpus();

        QueryEnhancer queryEnhancer = new QueryEnhancer(stemClasses);

        queryEnhancer.enhanceQuery("hello world query");

        */


        // ==============
        // TASK 3 : RUN
        // ==============


        Runner testRunTask3 = new Runner();


        //

    }
}
