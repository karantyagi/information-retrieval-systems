package com.ir.project.stemmer;

import com.ir.project.utils.Utilities;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class StemClassGenerator {

    private String corpusDocPath;
    private PorterStemmer porterStemmer;


    public StemClassGenerator(String corpusDocPath) {
        this.corpusDocPath = corpusDocPath;
        this.porterStemmer = new PorterStemmer();
    }

    public static void main(String argv[]) {

        String cleanedDocPath = "/tmp/irproject/";
        try {
            Map<String, Set<String>> stemClasses = new StemClassGenerator(cleanedDocPath).stemCorpus();
            System.out.println("Size is " + stemClasses.keySet().size());
            int i = 0;
            for (String word : stemClasses.keySet()) {
                System.out.println(word + ": " + stemClasses.get(word));
                i++;
                if (i == 100)
                    break;;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public Map<String, Set<String>> stemCorpus() throws FileNotFoundException {
        Map<String, Set<String>> stemClasses = new HashMap<>();

        Set<String> wordsInCorpus = fetchWordSetInDocument();

        for (String word : wordsInCorpus) {
            word = Utilities.processedWord(word);
            porterStemmer.setCurrent(word);
            porterStemmer.stem();
            String stemmedWord = porterStemmer.getCurrent();
            if (stemClasses.containsKey(stemmedWord)) {
                stemClasses.get(stemmedWord).add(word);
            } else {
                Set<String> wordList = new HashSet<>();
                wordList.add(word);
                stemClasses.put(stemmedWord, wordList);
            }
        }
        // prune stem classes
        return pruneStemClasses(stemClasses);
    }

    private Map<String,Set<String>> pruneStemClasses(Map<String, Set<String>> stemClasses) {

        Map<String,Set<String>> prunedStemClasses;
        Map<String,Set<String>> allStemClasses = stemClasses;
        String corpusPath = "src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "testcollection" +  File.separator + "cleanedcorpus";
        Map<String, String[]> docsAsWords = Utilities.corpusToWordList(corpusPath);

        int window = Utilities.TERM_ASSOCIATION_WINDOW;

        // for a pair in





        return prunedStemClasses;
    }

    private Set<String> fetchWordSetInDocument() throws FileNotFoundException {
        Set<String> wordSet = new HashSet<>();
        File corpusFolder = new File(this.corpusDocPath);
        for (File document : corpusFolder.listFiles()) {
            if (document.isFile()) {

                Scanner sc = new Scanner(document);
                while(sc.hasNext()){
                    String line = sc.nextLine();
                    line = Utilities.processedText(line);

                    if (line.length() > 0) {
                        for (String word: line.split(" ")) {
                            wordSet.add(word);
                        }
                    }
                }
            }
        }

        return wordSet;
    }

}
