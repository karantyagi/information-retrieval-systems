package com.ir.project.retrievalmodel;

import com.ir.project.utils.Utilities;

import java.io.File;
import java.io.IOException;
        import java.nio.file.Files;
        import java.nio.file.Paths;
        import java.util.*;

public class SnippetGenerator {

    public static List<String> getSummary(String docFilePath, String query) throws IOException {

        String docContents= new String(Files.readAllBytes(Paths.get(docFilePath)));
        String[] docSentence = Utilities.processedText(docContents).split("\n");
        return findSignificance(docSentence, query);
    }

    private static List<String> findSignificance(String[] sentence, String query) {

        Map<String, Double> scoreMap = new HashMap<>();
        List<String> snippetTopSentences = new ArrayList<>();
        List<String> queryTerms = Utilities.getQueryTerms(query);
        for(String s: sentence) {
            double count=0.0d;
            int startIndex= -1;
            int highIndex= -1;
            String word;
            String[] sen = Utilities.processedText(s).split(" ");
            for(int i=0; i< sen.length; i++) {
                word = Utilities.processedWord(sen[i]);
                if(queryTerms.contains(word)) {
                    if(startIndex == -1) {
                        startIndex = i;
                    }
                    highIndex = i;
                }
                count++;
            }
            if (startIndex > -1) {
                int total= (highIndex-startIndex) + 1;
                double score=0.0d;
                score=(total*total)/count;
                scoreMap.put(s,score);
            }
        }
        TreeMap<String, Double> sortedMap = new TreeMap<>((a,b)->{
            if(scoreMap.get(a) <= scoreMap.get(b)) return 1;
            else return -1;
        });
        for(String s : scoreMap.keySet()){
            sortedMap.put(s,scoreMap.get(s));
        }
        int countOfSnippets=0;
        // System.out.println(sortedMap);
        for (String name : sortedMap.keySet())
        {

            // TODO : this & significant words
            // significant words = query terms,  if more than 75% words are stop words
            // significant words = query terms  - stopwords, if stop words make less 75% of the query

            // what about docs with no sentences ???
            // e.g.
            // QUERY_ID: 18 , QL_NoStopNoStem
//            Query
//
//            Languages and compilers for parallel processors, especially highly
//            horizontal microcoded machines; code compaction

            for(String q : queryTerms) {
                if(!q.equals(name)){
                    name = name.replace(q, "<" + q + ">");
                }

            }
            snippetTopSentences .add(name);
            countOfSnippets++;
            if(countOfSnippets>=3)break;
        }
        ////System.out.print(snippetTopSentences );

        // ------------------
        //  Display Snippet
        // ------------------

        /*
        for(String sen : snippetTopSentences){
            System.out.println(sen);
        }
        */
        return snippetTopSentences;
    }



    public static void main(String[] args) throws IOException {

        String docDir = "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "testcollection" +  File.separator + "cleanedcorpus" + File.separator;

        List<String> snippetSentences = SnippetGenerator
                .getSummary(docDir+"CACM-2112.html_cleaned","Languages and compilers for parallel processors, especially highly\n" +
                        "horizontal microcoded machines; code compaction");




    }

}
