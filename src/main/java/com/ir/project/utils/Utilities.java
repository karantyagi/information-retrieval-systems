package com.ir.project.utils;

import com.ir.project.retrievalmodel.RetrievedDocument;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;

public class Utilities {

    public static final String WHITESPACE = "\\s"; // any whitespace character -  [ \t\n\x0B\f\r]+
    public static final String MULTIPLE_WHITESPACES = "//s"; // ????????????? mutliple whitespaces - regex
    public static int TERM_ASSOCIATION_WINDOW = 10;

    public static String processedWord(String word) {
        // remove all punctuations
        return word.replaceAll("[^\\p{ASCII}]", "")
                //.replaceAll("(?<![0-9a-zA-Z])[\\p{Punct}]", "")
                .replaceAll("(?<![0-9])[^\\P{P}-](?![0-9])", "") // retain hyphens in text
                .replaceAll("[\\p{Punct}](?![0-9a-zA-Z])", "")
                .replace("(", "")
                .replace(")", "")
                .replaceAll("/"," ")
                .replace("'s", "s");
    }

    public static String processedText(String line) {

        return (line.trim().toLowerCase()
                .replaceAll("\\("," ")
                .replaceAll("\\)"," ")
                .replaceAll("(\\r)", " "));
    }

    public static Map<String, String[]> corpusToWordList(String corpusPath) {
        Map<String, String[]> docTextMap = new HashMap<>();
        File cleanedCorpusDoc = new File(corpusPath);
        for (File f : cleanedCorpusDoc.listFiles()) {
            String words[] = new String[0];
            try {
                words = getWordListForDoc(f);
                String docId = f.getName().split("_")[0].split("\\.")[0];
                docTextMap.put(docId, words);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return docTextMap;
    }

    public static String[] getWordListForDoc(File doc) throws FileNotFoundException {
        List<String> words = new ArrayList<>();
        Scanner sc = new Scanner(doc);
        while(sc.hasNext()){
            String line = sc.nextLine();
            line = processedText(line);
            if (line.length() > 0) {

                for (String word : line.split(Utilities.WHITESPACE)) {
                    word = Utilities.processedWord(word);
                    words.add(word);
                }
            }
        }
        sc.close();
        String wordArray[] = new String[words.size()];
        words.toArray(wordArray);
        return wordArray;
    }

    public static List<String> getQueryTerms(String query) {

        // ------------------------------------------------------
        //  Add LOGIC for Pre-processing, improve pre-processing
        // ------------------------------------------------------

        List<String> queryTerms = new ArrayList<>();
        String terms[];
        query = query.trim();

        // ==== APPLY SAME PRE_PROCESSING to query also ======
        query = processedText(query);

        // split on any whitespace
        // Using java's predefined character classes

        terms = query.split(WHITESPACE);
        for(String t : terms) {
            // System.out.println("TERM: "+t);
            // add regex for 1 or more spaces e.g. " " or "    "
            t = processedWord(t);
            if(!(t.trim().equals("")) && !(t.trim().equals(MULTIPLE_WHITESPACES))){
                queryTerms.add(t.trim());
            }
            // else{
            //     System.out.println("------ Space -------");
            // }

        }
        return queryTerms;
    }


    public static void displayRetrieverdDoc(List<RetrievedDocument> retreivedDocs) {
        System.out.println("\nNo. of Docs scored: "+ retreivedDocs.size()+"\n");
        retreivedDocs.forEach(doc->System.out.println(doc.toString()));
    }

    public static Map<Integer, List<String>> fetchQueryRelevantDocList(String filePath)
            throws FileNotFoundException {
        Map<Integer, List<String>> queryRelevantDocList = new HashMap<>();


        File f = new File(filePath);

        Scanner sc = new Scanner(f);
        while (sc.hasNext()) {
            //1 Q0 CACM-1410 1
            String line = sc.nextLine();
            Integer queryId = Integer.parseInt(line.split(" ")[0]);
            String docId = line.split(" ")[2];

            if (!queryRelevantDocList.containsKey(queryId)) {
                queryRelevantDocList.put(queryId, new ArrayList<>());
            }
            queryRelevantDocList.get(queryId).add(docId);
        }
        sc.close();
        return queryRelevantDocList;
    }

    public static List<String> getStopWords() throws IOException {

        List<String> stopWordList = new ArrayList<>();
        String stopWordsFilePath = "src" + File.separator
                + "main" + File.separator + "resources" + File.separator
                + "testcollection" + File.separator + "common_words";

        try {

            FileInputStream fstream = new FileInputStream(stopWordsFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String line;
            String fullQuery = "";
            int qID = 0;
            String words[];

            //Read File Line By Line
            while ((line = br.readLine()) != null) {
                stopWordList.add(Utilities.processedWord(line).trim());
            }

            //Close the input stream
            br.close();
            fstream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // print out stopwords list
        //stopWordList.forEach(w->System.out.println(w));
    return stopWordList;
    }

}
