package com.ir.project.retrievalmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.retrievalmodel.bm25retrieval.BM25;
import com.ir.project.retrievalmodel.luceneretrieval.LuceneRetrievalModel;
import com.ir.project.retrievalmodel.querylikelihoodretrieval.QLModel;
import com.ir.project.retrievalmodel.tfidfretrieval.TFIDF;
import com.ir.project.utils.SearchQuery;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.ir.project.utils.Utilities.getQueryTerms;

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
        System.out.println("\n --------------------------------- TFID Retrieval Run complete ------------------------------\n");

        runBM25Model(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        System.out.println("\n --------------------------------- BM25 Retrieval Run complete ------------------------------\n");

        runQueryLikelihoodModel(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        System.out.println("\n ------------------------ Smoothed Query Likelihood Retrieval Run complete ------------------\n");

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



    public List<SearchQuery> fetchSearchQueries(@NotNull String queryFilePath) throws IOException {
        List<SearchQuery> searchQueryList = new ArrayList<>();

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(queryFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String line;
            String fullQuery = "";
            String words[];
            SearchQuery temp = new SearchQuery(0,"");

            //Read File Line By Line
            while ((line = br.readLine())!= null)   {
                System.out.println("Line: "+line);
                words = line.split(" ");
                if(words.length==0){
                    System.out.println("No words. -------- ");
                }
                else {
                    System.out.println("Words: "+words.length+" | "+words[0]);

                    if(words[0].equals("<DOCNO>")){
                        temp.setQueryID(Integer.parseInt(words[1]));
                        searchQueryList.add(temp);
                        System.out.println(temp.toString());
                        System.out.println(" ------------------- QUERY_ID: "+temp.getQueryID()+" added.");
                    }



                }

                /*
                if(words.length>1 && words[0].equals("<DOC>")){
                    fullQuery = "";
                }
                else if(words.length>1 && words[1].equals("<DOCNO>")) {
                    temp.setQueryID(Integer.parseInt(words[1]));
                }

                else if(words.length>1 && words[1].equals("</DOCNO>")) {
                    temp.setQuery(fullQuery);
                    searchQueryList.add(temp);
                    System.out.println(temp.toString());
                    fullQuery = "";
                }

                else{ fullQuery = fullQuery + line + "\n";
                    //System.out.print("\t\t  ------ Line added");
                    }
                */

            }
            //Close the input stream
            br.close();
            fstream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




        // TODO add fecth from file logic

        return searchQueryList;
    }

    public static void main(String args[]) throws IOException {

        System.out.println();

        Runner testRun = new Runner();

        String queryText = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        int queryID =1;
        SearchQuery testQuery = new SearchQuery(queryID,queryText);

        String queriesFilePath = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cacm.query.txt";

        List<SearchQuery> queries1 = testRun.fetchSearchQueries(queriesFilePath);
        List<SearchQuery> queries = new ArrayList<>();
        // ------- comment the below out -------
        ////queries.add(testQuery);

        // ==============================================================
        // Run 1,2,3: TFIDFNoStopNoStem, BM25NoStopNoStem, QLNoStopNoStem
        // ==============================================================

        ////testRun.run(queries);

        // ==========================
        // Run 4: LuceneNoStopNoStem
        // ==========================

        Runner testRunLucene = new Runner(RetrievalModelType.LUCENE.name());
        LuceneRetrievalModel runLucene = new LuceneRetrievalModel();
        String luceneIndexDirPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "luceneindex" +  File.separator;
        runLucene.loadIndex(luceneIndexDirPath);
        String outFile = "src" + File.separator + "main" + File.separator + "output" + File.separator;
        ////testRunLucene.runLucene(queries,RetrievalModelRun.NoStopNoStem.name(),outFile);
        ////System.out.println("\n ------------------------ Lucene(default settings) Retrieval Run complete -------------------\n");

    }
}
