package com.ir.project.luceneretrieval;

import com.ir.project.retrievalmodel.RetrievalModel;
import com.ir.project.retrievalmodel.SearchedDocument;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LuceneRetrievalModel implements RetrievalModel {

    private static Analyzer simpleAnalyzer;
    private static IndexReader reader;
    private static IndexSearcher searcher;

    /**
     * @param indexDir path of index directory
     * @throws java.io.IOException when exception loading index.
     */
    public static void loadIndex(String indexDir) throws IOException {

        simpleAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);
        reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        searcher = new IndexSearcher(reader);
    }


    /**
     * @param query
     * @return List of retrieved documents after running Lucene retrieval model on a given query
     */
    public Set<SearchedDocument> search(String query) throws IOException {

        Set<SearchedDocument> retrievedDocs = new HashSet<>();
        TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
        Query q;
        ScoreDoc[] hits;

        try {
            q = new QueryParser(Version.LUCENE_47, "contents", simpleAnalyzer).parse(query);
            searcher.search(q, collector);
            hits = collector.topDocs().scoreDocs;
            retrievedDocs = getSearchedDocsList(hits);

        } catch (Exception e) {
            System.out.println("Error searching " + query + " : " + e.getMessage());
        }
        reader.close();
        return retrievedDocs;
    }


    private static Set<SearchedDocument> getSearchedDocsList(ScoreDoc[] hits) throws IOException {

        Set<SearchedDocument> retrievedDocList = new HashSet<>();
        System.out.println("Found " + hits.length + " hits.\n");
        for (int i = 0; i < hits.length; ++i) {
            int luceneDocID = hits[i].doc;
            Document d = searcher.doc(luceneDocID);
            String docID = d.get("path").toString();
            docID = docID.substring(0, docID.lastIndexOf('.'));
            SearchedDocument s = new SearchedDocument(docID);
            s.updateScore(hits[i].score);
            System.out.printf("%-3d  %-85s       Rank: %-3d | Score: %1.7f \n", (i + 1), s.docID(), s.rank(), s.score());
            retrievedDocList.add(s);
        }

        // sort and update ranks

        // ---------------------------------
        // Try and optimize this code later
        // ---------------------------------



        return retrievedDocList;
    }

}
