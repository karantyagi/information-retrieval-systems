package com.ir.project.indexer;

import com.ir.project.utils.Utilities;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class FileIndexerThread implements Callable <Pair<String, Map<String, Integer>>> {

    private String filePath;
    private List<String> stopList;

    private FileIndexerThread() {
    }

    public FileIndexerThread(String filePath) {
        this.filePath = filePath;
    }

    public FileIndexerThread(String filePath, List<String> stopList) {
        this.filePath = filePath;
        this.stopList = stopList;
    }

    public Pair<String, Map<String, Integer>> call() throws Exception {

        Map<String, Integer> wordMap = new HashMap<String, Integer>();
        File file = new File(filePath);

        Scanner sc = new Scanner(file);
        while(sc.hasNext()){
            String line = sc.nextLine();

            if (line.length() > 0) {

                for (String word : line.split(Utilities.WHITESPACE)) {
                    word = Utilities.processedWord(word);

                    if (stopList != null && stopList.contains(word))
                        continue;

                    if(!(word.trim().equals("")) && !(word.trim().equals(Utilities.MULTIPLE_WHITESPACES))) {
                        int count = wordMap.containsKey(word) ? wordMap.get(word) : 0;
                        wordMap.put(word, count + 1);
                    }
                }
            }
        }
        sc.close();
        String docId = file.getName().split("\\.")[0];
        return new Pair<String, Map<String, Integer>>(docId,wordMap);
    }

    public String getFilePath() {
        return filePath;
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<String> getStopList() {
        return stopList;
    }

    private void setStopList(List<String> stopList) {
        this.stopList = stopList;
    }
}
