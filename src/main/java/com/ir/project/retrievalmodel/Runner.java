package com.ir.project.retrievalmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.extracredit.ECTask1;
import com.ir.project.extracredit.ECTask2;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.indexer.StemmedIndexer;
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
import org.apache.commons.cli.*;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;


public class Runner {

    private DocMetadataAndIndex metadataAndIndex;

    public Runner(String luceneModel) throws FileNotFoundException {}   // for loading lucene index


    public Runner(RetrievalModelRun systemRun) throws FileNotFoundException {
                ObjectMapper om = new ObjectMapper();
                try {
                    String indexPath;
                    switch (systemRun) {
                        case NoStopNoStem:
                            indexPath = "src" + File.separator + "main"
                                    + File.separator + "resources" + File.separator
                                    + "invertedindex" +  File.separator + "metadata.json";
                            System.out.println("RUN: "+systemRun.name()+" . . . inverted index loaded!");
                            this.metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
                            break;
                        case WithStopNoStem:
                            indexPath = "src" + File.separator + "main"
                                    + File.separator + "resources" + File.separator
                                    + "stoppedindex" +  File.separator + "metadata.json";
                            System.out.println("RUN: "+systemRun.name()+" . . . stopped inverted index loaded!");
                            this.metadataAndIndex = om.readValue(new File(indexPath), DocMetadataAndIndex.class);
                            break;
                        case NoStopWithStem:

                            String stemmedFilePath = "src" + File.separator + "main"
                                    + File.separator + "resources" + File.separator
                                    + "testcollection" + File.separator + "cacm_stem.txt";
                            this.metadataAndIndex = StemmedIndexer.generateIndex(stemmedFilePath);
                            break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
        }

    public void run(List<SearchQuery> queries, RetrievalModelRun systemName, Map<Integer, List<String>> relevantQueryDocMap, boolean generateSnippet) {
        //TODO:
        long start;
        long elapsed;
        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator;
        String snippetDir = "src" + File.separator + "main" + File.separator + "snippets" + File.separator;

        start = System.currentTimeMillis();
        runTFIDFModel(queries,systemName.name(),outFile,snippetDir, relevantQueryDocMap, generateSnippet);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n --------------------------------- TFID Retrieval Run complete ------------------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

        start = System.currentTimeMillis();
        runBM25Model(queries,systemName.name(),outFile,snippetDir, relevantQueryDocMap, generateSnippet);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n --------------------------------- BM25 Retrieval Run complete ------------------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

        start = System.currentTimeMillis();
        runQueryLikelihoodModel(queries,systemName.name(),outFile,snippetDir, relevantQueryDocMap, generateSnippet);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n ------------------------ Smoothed Query Likelihood Retrieval Run complete ------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

    }

    private void runLucene(List<SearchQuery> queries, RetrievalModelRun systemRun,String outputDir,String snippetDir,Map<Integer, List<String>> relevantQueryDocMap, boolean generateSnippet) {
        RetrievalModel lucene = new LuceneRetrievalModel();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(lucene, q, outputDir,snippetDir, systemRun.name(), generateSnippet);
            Future<Pair<SearchQuery, List<RetrievedDocument>>> f = executor.submit(task);
            futures.add(f);
        }

        executor.shutdown();
        Map<SearchQuery, List<RetrievedDocument>> queriesAndDocs = pollForCompletion(futures);
        evaluateAndPrintStats(relevantQueryDocMap, queriesAndDocs, RetrievalModelType.LUCENE, systemRun.name());

    }

    private void runTFIDFModel(List<SearchQuery> queries, String systemRunName, String outputDir, String snippetDir,Map<Integer, List<String>> relevantQueryDocMap, boolean generateSnippet) {
        try {
            RetrievalModel tfidf = new TFIDF(metadataAndIndex);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(tfidf, q, outputDir,snippetDir,systemRunName, generateSnippet);
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

    private void runBM25Model(List<SearchQuery> queries, String systemRunName, String outputDir, String snippetDir,Map<Integer, List<String>> relevantQueryDocMap, boolean generateSnippet) {
        double k1 = 1.2;
        double b = 0.75;
        double k2 = 100;

        RetrievalModel bm25 = null;
        try {
            bm25 = new BM25(this.metadataAndIndex, k1, k2, b);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

            for(SearchQuery q : queries) {
                RetrievalTask task = new RetrievalTask(bm25, q, outputDir,snippetDir,systemRunName, generateSnippet);
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

    private void runQueryLikelihoodModel(List<SearchQuery> queries, String systemRunName,
                                         String outputDir, String snippetDir,Map<Integer, List<String>> relevantQueryDocMap, boolean generateSnippet) {

        try
        {
        double smoothingFactor = 0.35;
        RetrievalModel queryLikelihoodModel = new QLModel(metadataAndIndex, smoothingFactor);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Pair<SearchQuery, List<RetrievedDocument>>>> futures = new ArrayList<>();

        for(SearchQuery q : queries) {
            RetrievalTask task = new RetrievalTask(queryLikelihoodModel, q, outputDir,snippetDir, systemRunName, generateSnippet);
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
            List<RetrievedDocument>> queriesAndDocs, RetrievalModelType retrievalModelType, String systemName) {
        String relevantDocFilePath =
                "src" + File.separator + "main" +
                File.separator + "evaluation" +
                File.separator + retrievalModelType.name() +
                File.separator + systemName;

        Evaluator evaluator = new Evaluator(retrievalModelType.name(), relevantQueryDocMap, queriesAndDocs);
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


    public static void main(String args[]) throws ParseException, IOException {

        Options options = new Options();

        options.addOption("taskName", true, "task to run [ can be one of the TASK1, TASK2 or TASK3, PHASE1, PHASE2, noiseGeneration, softMatching]");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);


        String taskName = null;
        if (cmd.hasOption("taskName")) {
            taskName = cmd.getOptionValue("taskName").toUpperCase();
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "Retreival Model", options );
            System.exit(1);
        }

        if (taskName.equals("TASK1")) {
            runTask1(false);
        } else if (taskName.equals("TASK2")) {
            runTask2(false);
        } else if (taskName.equals("TASK3")) {
            runTask3(false);
        } else if (taskName.equals("PHASE1")) {
            runTask1(false);
            runTask2(false);
            runTask3(false);
        } else if (taskName.equals("PHASE2")) {
            runPhase2(true);
        } else if (taskName.equals("SOFTMATCHING")) {
            runSoftMatching();

        } else if (taskName.equals("NOISEGENERATION")) {
            runWithNoiseGeneration();
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "Retreival Model", options );
            System.exit(1);
        }

    }

    // runs for base line models for Phase 1 task 1.
    public static void runTask1(boolean generateSnippet) throws IOException {
        long start = 0;
        long elapsed =0;

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";

        start = System.currentTimeMillis();
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded relevence Judgements - Run Time : " + elapsed + " milliseconds");


        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";


        // ==============================================================
        // Run 1,2,3: TFIDFNoStopNoStem, BM25NoStopNoStem, QLNoStopNoStem
        // ==============================================================

        Runner testRunTask1 = new Runner(RetrievalModelRun.NoStopNoStem); // Stopping with no stemming
        List<SearchQuery> queries = testRunTask1.fetchSearchQueries(queriesFilePath);
        testRunTask1.run(queries,RetrievalModelRun.NoStopNoStem, relevantQueryDocMap, generateSnippet);

        // ==========================
        // Run 4: LuceneNoStopNoStem
        // ==========================

        // Initializing Lucene


        Runner testRunLucene = new Runner(RetrievalModelType.LUCENE.name());
        LuceneRetrievalModel runLucene = new LuceneRetrievalModel();
        String luceneIndexDirPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "luceneindex" +  File.separator;
        start = System.currentTimeMillis();
        runLucene.loadIndex(luceneIndexDirPath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded lucene index  - Run Time : " + elapsed + " milliseconds");

        String outFile =    "src" + File.separator + "main" + File.separator + "output" + File.separator;
        String snippetDir = "src" + File.separator + "main" + File.separator + "snippets" + File.separator;

        start = System.currentTimeMillis();

        testRunLucene.runLucene(queries,RetrievalModelRun.NoStopNoStem,outFile,snippetDir, relevantQueryDocMap, generateSnippet);

        runLucene .closeIndex();
        elapsed = System.currentTimeMillis() - start;

        System.out.println("\n ------------------------ Lucene(default settings) Retrieval Run complete -------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds");

    }


    // running query enhancement on BM25 using query time stemming.
    public static void runTask2(boolean generateSnippet) throws IOException {

        long start = 0;
        long elapsed =0;

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";

        start = System.currentTimeMillis();
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded relevence Judgements - Run Time : " + elapsed + " milliseconds");


        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";
        Runner testRunTask2 = new Runner(RetrievalModelRun.NoStopNoStem); // Stopping with no stemming
        List<SearchQuery> queries = testRunTask2.fetchSearchQueries(queriesFilePath);


        String cleanedCorpusDocPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                "testcollection" + File.separator + "cleanedcorpus";

        // generate stem classes
        Map<String, Set<String>> stemClasses = new StemClassGenerator(cleanedCorpusDocPath).stemCorpus();

        // prune stem classes
        //stemClasses = StemClassGenerator.pruneStemClasses(stemClasses);

        String stemClassesFilePath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                "testcollection" + File.separator + "stemclasses.json";

        //StemClassGenerator.saveStemClassesToFile(stemOutFile, stemClasses);

        // Expand query using query stems..
        Map<String, Set<String>> semClasses = StemClassGenerator.getStemClasses(stemClassesFilePath);

        QueryEnhancer queryEnhancer = new QueryEnhancer(stemClasses);
        for (SearchQuery searchQuery : queries) {
            searchQuery.setQuery(queryEnhancer.enhanceQuery(searchQuery.getQuery()));
        }

        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator;
        String snippetDir = "src" + File.separator + "main" + File.separator + "snippets" + File.separator;

        start = System.currentTimeMillis();
        testRunTask2.runBM25Model(queries, RetrievalModelRun.NoStopNoStem.name(), outFile, snippetDir, relevantQueryDocMap, generateSnippet);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n --------------------------------- BM25 Retrieval Run complete ------------------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

    }


    public static void  runTask3(boolean generateSnippet) throws IOException {
        long start = 0;
        long elapsed =0;

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";

        start = System.currentTimeMillis();
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded relevence Judgements - Run Time : " + elapsed + " milliseconds");


        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";


        // Run 1,2,3: TFIDFWithStopNoStem, BM25NoWithStopNoStem, QLWithStopNoStem
        // Stopping with no stemming

        Runner testRunTask3A = new Runner(RetrievalModelRun.WithStopNoStem);
        List<SearchQuery> queries = testRunTask3A.fetchSearchQueries(queriesFilePath);

        testRunTask3A.run(queries,RetrievalModelRun.WithStopNoStem,relevantQueryDocMap, generateSnippet);

        // Run 4,5,6:

        // Index the stemmed version of	the	corpus (cacm_stem.txt)

        Runner testRunTask3B = new Runner(RetrievalModelRun.NoStopWithStem);
        testRunTask3B.run(queries,RetrievalModelRun.NoStopWithStem,relevantQueryDocMap, generateSnippet);
    }


    public static void runPhase2(boolean generateSnippet) throws IOException {

        long start = 0;
        long elapsed =0;

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";

        start = System.currentTimeMillis();
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded relevence Judgements - Run Time : " + elapsed + " milliseconds");


        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";
        Runner testRunTask2 = new Runner(RetrievalModelRun.NoStopNoStem); // Stopping with no stemming
        List<SearchQuery> queries = testRunTask2.fetchSearchQueries(queriesFilePath);


        String cleanedCorpusDocPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                "testcollection" + File.separator + "cleanedcorpus";


        String stemClassesFilePath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                "testcollection" + File.separator + "stemclasses.json";

        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator + "phase2" + File.separator;
        String snippetDir = "src" + File.separator + "main" + File.separator + "snippets" + File.separator;

        start = System.currentTimeMillis();
        testRunTask2.runBM25Model(queries, RetrievalModelRun.NoStopNoStem.name(), outFile, snippetDir, relevantQueryDocMap, generateSnippet);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("\n --------------------------------- BM25 Retrieval Run complete ------------------------------");
        System.out.println("Run Time : " + elapsed + " milliseconds\n");

    }

    public static void runWithNoiseGeneration() throws IOException {
        System.out.println();
        long start = 0;
        long elapsed =0;

        // initialize Test Collection - Releance Judgements and Query file

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";

        start = System.currentTimeMillis();
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded relevence Judgements - Run Time : " + elapsed + " milliseconds");


        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";

        Runner testRunTask1 = new Runner(RetrievalModelRun.NoStopNoStem); // Stopping with no stemming
        List<SearchQuery> noisyQueries = testRunTask1.fetchSearchQueries(queriesFilePath);

        noisyQueries = ECTask1.returnNoisySearchQuery(noisyQueries);
        testRunTask1.run(noisyQueries,RetrievalModelRun.NoStopNoStem, relevantQueryDocMap, true);

    }


    public static void runSoftMatching() throws IOException {
        System.out.println();
        long start = 0;
        long elapsed =0;

        // initialize Test Collection - Relevance Judgements and Query file

        String relevantDocFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.rel.txt";

        start = System.currentTimeMillis();
        Map<Integer, List<String>> relevantQueryDocMap = Utilities.fetchQueryRelevantDocList(relevantDocFilePath);
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded relevence Judgements - Run Time : " + elapsed + " milliseconds");


        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";
        Runner testRunTask1 = new Runner(RetrievalModelRun.NoStopNoStem); // Stopping with no stemming
        List<SearchQuery> Noisyqueries = testRunTask1.fetchSearchQueries(queriesFilePath);

        List<SearchQuery> correctedQuery= new ECTask2(testRunTask1.metadataAndIndex).getMitigatedQuaries(Noisyqueries);
        testRunTask1.run(Noisyqueries,RetrievalModelRun.NoStopNoStem, relevantQueryDocMap, true);
    }
}
