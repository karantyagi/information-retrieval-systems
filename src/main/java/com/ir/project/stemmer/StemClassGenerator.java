package com.ir.project.stemmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.utils.Utilities;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class StemClassGenerator {

    private String corpusDocPath;
    private PorterStemmer porterStemmer;


    public StemClassGenerator(String corpusDocPath) {
        this.corpusDocPath = corpusDocPath;
        this.porterStemmer = new PorterStemmer();
    }

    public static void main(String argv[]) {

        try {

        String cleanedDocPath = "src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "testcollection" +  File.separator + "cleanedcorpus";

            // generate stem classes
            StemClassGenerator s = new StemClassGenerator(cleanedDocPath);
            Map<String, Set<String>> stemClasses = s.stemCorpus();

            System.out.println("\n");

            /*
            Map<String, Set<String>> subStemClasses  =
                    stemClasses.entrySet()
                            .stream()
                            .filter(p -> (p.getValue().size()>10))
                            .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
                            */

            // prune stem classes
            stemClasses = StemClassGenerator.pruneStemClasses(stemClasses);

            String stemOutFile = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                    "testcollection" + File.separator + "stemclasses.json";

            StemClassGenerator.saveStemClassesToFile(stemOutFile, stemClasses);
            /*
            System.out.println("Printing pruned stem classes\n");
            for (String word : stemClasses.keySet()) {
                if(stemClasses.get(word).size()>1){
                    System.out.printf("%-10s : %s\n",word,stemClasses.get(word));
                }
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Map<String, Set<String>> stemCorpus() throws FileNotFoundException {
        Map<String, Set<String>> stemClasses = new HashMap<>();

        Set<String> wordsInCorpus = fetchWordSetInDocument();

        for (String word : wordsInCorpus) {
            word = Utilities.processedWord(word);
            if(word.length()>=1){
                porterStemmer.setCurrent(word);
                porterStemmer.stem();
                String stemmedWord = porterStemmer.getCurrent();
                if (stemmedWord.length() > 0 && !stemmedWord.equals(word)) {
                    if (stemClasses.containsKey(stemmedWord)) {
                        stemClasses.get(stemmedWord).add(word);
                    } else {
                        Set<String> wordList = new HashSet<>();
                        wordList.add(word);
                        stemClasses.put(stemmedWord, wordList);
                    }
                }
            }

        }
        /*
         prune stem classes
        if(stemClasses.size()>2){
            return pruneStemClasses(stemClasses);
        }
        else{
            return stemClasses;
        }
        */
        return stemClasses;
    }

    public static Map<String,Set<String>> pruneStemClasses(Map<String, Set<String>> stemClasses) {
        double size = stemClasses.size();
        int count =0;
        Map<String,Set<String>> prunedStemClasses = new HashMap<>();
        for (Map.Entry<String, Set<String>> termStemCLasses : stemClasses.entrySet()) {
            count++;
            if(termStemCLasses.getValue().size()>2){
                prunedStemClasses.put(termStemCLasses.getKey(), new HashSet<>());

                Map<String, Double> associationScores = new HashMap<>();

                String corpusPath = "src" + File.separator + "main" + File.separator
                        + "resources" + File.separator + "testcollection" +  File.separator + "cleanedcorpus";

                Map<String, String[]> docsAsWords = Utilities.corpusToWordList(corpusPath);

                // CHECK FOR SAME PREPROCESSING

                String word1 = "";
                String word2 = "";

                // convert set to list
                List<String> stemClassesList = new ArrayList<>(termStemCLasses.getValue());
                ////System.out.println("Put term in prunedStemClass : " + termStemCLasses.getValue());
                String[] largeStemCLass = new String[stemClassesList.size()];
                largeStemCLass = stemClassesList.toArray(largeStemCLass);

                //String[] largeStemCLass = stemClasses.toArray(new String[stemClasses.size()]);


                //System.out.println("Stem class array: "+ largeStemCLass.toString());
                for(int i=0;i<largeStemCLass.length;i++) {
                    word1 = largeStemCLass[i];
                    for (int j = i + 1; j < largeStemCLass.length; j++) {
                        word2 = largeStemCLass[j];
                        word1 = Utilities.processedWord(word1);
                        word2 = Utilities.processedWord(word2);
                        // make string pair
                        //System.out.println("PAIR: "+word1+"\t"+word2);
                        String wordPair = createPair(word1, word2);
                        associationScores.put(wordPair, calculateDiceCoefficient(wordPair, docsAsWords));
                        // print word pair
                        // System.out.println("1: "+word1+" 2: "+word2);
                    }
                }


                // Print association(dice's co-efficient) scores:

                //associationScores.forEach((k,v)->System.out.println("Pair : " + k + " Score : " + v));

                // get pair with max co-occurrence measure - dice's co-efficient
                String maxPair = Collections.max(associationScores.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
                //System.out.println("MAX PAIR :"+ maxPair +"  score: "+associationScores.get(maxPair));
                // return pair words of max score classes


                prunedStemClasses.get(termStemCLasses.getKey()).add(maxPair.split(" ")[0]);
                prunedStemClasses.get(termStemCLasses.getKey()).add(maxPair.split(" ")[1]);
            }

            else{
                prunedStemClasses.put(termStemCLasses.getKey(), termStemCLasses.getValue());
            }
            System.out.printf("\r Progress: %2.2f %% ", (count*100)/size);

        }
        System.out.println("\n");

        return prunedStemClasses;
    }

    private static Double calculateDiceCoefficient(String wordPair, Map<String, String[]> docsAsWords) {
        int n1 = 0;
        int n2 = 0;
        int n12 = 0;
        String word1 = wordPair.split(" ")[0];
        String word2 = wordPair.split(" ")[1];
        Boolean word1InWindow;
        Boolean word2InWindow;
        Boolean word1word2InWindow;
        String[] docWords;
        for (Map.Entry<String, String[]> doc : docsAsWords.entrySet()) {
            docWords = doc.getValue();
            int windowFront = 0;
            int windowRear = windowFront + Utilities.TERM_ASSOCIATION_WINDOW - 1;
            while(windowRear<docWords.length){
                word1InWindow = false;
                word2InWindow = false;
                for(int i= windowFront; i<= windowRear; i++){
                    if(word1.equals(docWords[i])){
                        word1InWindow = true;
                    }
                    if(word2.equals(docWords[i])){
                        word2InWindow = true;
                    }
                }

                // end checking this window
                if(word1InWindow){n1++;}
                if(word2InWindow){n2++;}
                if(word1InWindow && word2InWindow){ n12++;}

                // advance window ahead
                windowFront++;
                windowRear++;
            }
        }

        return (double)(2*n12)/(double)(n1+n2);
    }

    private static String createPair(String word1, String word2) {
        if(word1.compareTo(word2)>0){
            return word1+" "+word2;
        }
        else
            return word2+" "+word1;
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


    public static void saveStemClassesToFile(String filePath, Map<String,Set<String>> stemClasses) {
        ObjectMapper om = new ObjectMapper();
        try {
            Files.write(Paths.get(filePath), om.writeValueAsBytes(stemClasses));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,Set<String>> getStemClasses(String filePath) {
        Map<String,Set<String>> map = new HashMap<>();
        ObjectMapper om = new ObjectMapper();

        try {
            File file = new File(filePath);
            map = om.readValue(file, map.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

}
