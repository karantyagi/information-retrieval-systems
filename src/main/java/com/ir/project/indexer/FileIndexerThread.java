package com.ir.project.indexer;

import com.ir.project.utils.Utilities;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class FileIndexerThread implements Callable <Pair<String, Map<String, Integer>>> {

    private String filePath;
    private static final String WHITESPACE = "\\s";  // any whitespace character -  [ \t\n\x0B\f\r]
    private static final String MULTIPLE_WHITESPACES = "//s"; // ????????????? mutliple whitespaces - regex

    private FileIndexerThread() {
    }

    public FileIndexerThread(String filePath) {
        this.filePath = filePath;
    }

    public Pair<String, Map<String, Integer>> call() throws Exception {

        Map<String, Integer> wordMap = new HashMap<String, Integer>();

        File file = new File(filePath);

        Scanner sc = new Scanner(file);
        while(sc.hasNext()){
            String line = sc.nextLine();

            if (line.length() > 0) {

                for (String word : line.split(WHITESPACE)) {
                    word = Utilities.processedWord(word);
                    if(!(word.trim().equals("")) && !(word.trim().equals(MULTIPLE_WHITESPACES))) {
                        int count = wordMap.containsKey(word) ? wordMap.get(word) : 0;
                        wordMap.put(word, count + 1);
                    }
                }
            }
        }
        String docId = file.getName().split("\\.")[0];
        return new Pair<String, Map<String, Integer>>(docId,wordMap);
    }
}
