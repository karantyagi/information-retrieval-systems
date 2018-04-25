package com.ir.project.indexer;

import com.ir.project.utils.Utilities;
import javafx.geometry.Pos;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class StemmedIndexer {

    private StemmedIndexer(){
    }

    public static DocMetadataAndIndex generateIndex(@NotNull String stemmedIndexFilePath) throws FileNotFoundException {
        File indexFile = new File(stemmedIndexFilePath);
        Map<String, Map<String, Integer>> documentWordMap = new HashMap<>();
        Scanner sc = new Scanner(indexFile);
        String currentDocId = null;

        while(sc.hasNext()){

            String line = sc.nextLine();
            if (line.length() > 0) {

                if (line.startsWith("#")) {
                    line = line.replace("# ", "CACM-");
                    currentDocId = line;
                    documentWordMap.put(currentDocId, new HashMap<>());

                } else {
                    for (String word : line.split(Utilities.WHITESPACE)) {
                        word = Utilities.processedWord(word);
                        if(!(word.trim().equals("")) && !(word.trim().equals(Utilities.MULTIPLE_WHITESPACES))) {
                            int count = documentWordMap.get(currentDocId).containsKey(word) ?
                                    documentWordMap.get(currentDocId).get(word) : 0;
                            documentWordMap.get(currentDocId).put(word, count + 1);
                        }
                    }
                }
            }
        }

        sc.close();

        DocMetadataAndIndex docMetadataAndIndex = generateDocMetadataAndIndexFromDocumentWordMap(documentWordMap);
        return docMetadataAndIndex;
    }


    private static DocMetadataAndIndex generateDocMetadataAndIndexFromDocumentWordMap(Map<String, Map<String, Integer>> documentWordMap) {
        Map<String,List<Posting>> index = new HashMap<>();
        Map<String,Integer> documentLenghtMap = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> documentWordMapEntry : documentWordMap.entrySet()) {
            String docId = documentWordMapEntry.getKey();
            Integer totalLength = 0;
            Map<String, Integer> termFreqMap = documentWordMapEntry.getValue();

            for (Map.Entry<String, Integer> termEntry : termFreqMap.entrySet()) {
                String term = termEntry.getKey();
                Integer freq = termEntry.getValue();

                totalLength+=freq;
                Posting posting = new Posting(docId, freq);

                if (index.containsKey(term)) {
                    index.get(term).add(posting);
                } else {
                    List<Posting> newList = new ArrayList<>();
                    newList.add(posting);
                    index.put(term, newList);
                }
            }

            documentLenghtMap.put(docId, totalLength);
        }

        return new DocMetadataAndIndex(index, documentLenghtMap);
    }

// Test using Main
    /*
    public static void main(String args[]) {
        String stemDocPath= "src" + File.separator + "main" + File.separator +
                "resources" + File.separator + "testcollection" + File.separator + "cacm_stem.txt";
        try {
            DocMetadataAndIndex docMetadataAndIndex = generateIndex(stemDocPath);
            System.out.println("Stemmed Inverted index created!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    */
}

