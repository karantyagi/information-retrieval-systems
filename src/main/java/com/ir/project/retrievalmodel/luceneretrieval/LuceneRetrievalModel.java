package com.ir.project.retrievalmodel.luceneretrieval;

import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.RetrievedDocument;

import com.ir.project.utils.SearchQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LuceneRetrievalModel implements RetrievalModel {

    private static Analyzer simpleAnalyzer;
    private static IndexReader reader;
    private static IndexSearcher searcher;

    /**
     * @param indexDir path of index directory
     * @throws java.io.IOException when exception loading index.
     */
    public void loadIndex(String indexDir) throws IOException {

        simpleAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);
        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        searcher = new IndexSearcher(reader);
    }


    /**
     * @param query
     * @return List of retrieved documents after running Lucene retrieval model on a given query
     */
    public List<RetrievedDocument> search(SearchQuery query) throws IOException {

        List<RetrievedDocument> retrievedDocs = new ArrayList<>();
        TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
        Query q;
        ScoreDoc[] hits;

        try {
            q = new QueryParser(Version.LUCENE_47, "contents", simpleAnalyzer).parse(query.getQuery());
            searcher.search(q, collector);
            hits = collector.topDocs().scoreDocs;
            retrievedDocs = getSearchedDocsList(hits);

        } catch (Exception e) {
            System.out.println("Error searching " + query + " : " + e.getMessage());
        }
        reader.close();
        return retrievedDocs;
    }


    private static List<RetrievedDocument> getSearchedDocsList(ScoreDoc[] hits) throws IOException {

        List<RetrievedDocument> retrievedDocList = new ArrayList<>();
        System.out.println("\nFound " + hits.length + " hits.\n");
        for (int i = 0; i < hits.length; ++i) {
            int luceneDocID = hits[i].doc;
            Document d = searcher.doc(luceneDocID);
            String docID = d.get("path").toString();
            docID = docID.substring(docID.lastIndexOf('\\') + 1);
            docID = docID.substring(0, docID.lastIndexOf("."));
            RetrievedDocument s = new RetrievedDocument(docID);
            s.setScore(hits[i].score);
            System.out.printf(" %-10s  |  Rank: %-3d  |  Score: %1.7f \n", s.getDocumentID(), i+1, s.getScore());
            retrievedDocList.add(s);
        }

        // hits are already sorted
        return retrievedDocList;
    }

    public static void  main(String args[]) throws IOException {

        String queryText = "What articles exist which deal with TSS (Time Sharing System), an\n" +
                "operating system for IBM computers?";
        int queryID = 1;
        SearchQuery testQuery = new SearchQuery(queryID,queryText);
        LuceneRetrievalModel test = new LuceneRetrievalModel();

        test.loadIndex("E:\\1st - Career\\NEU_start\\@@Technical\\2 - sem\\IR\\Karan_Tyagi_Project\\lucene-index");

        test.search(testQuery);
    }

}
