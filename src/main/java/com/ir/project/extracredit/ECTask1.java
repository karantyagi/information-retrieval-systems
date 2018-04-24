package com.ir.project.extracredit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ECTask1 {
    public List<String> bucket;
    public String makeBucket(String query){
    	   String tempQuery = query.toLowerCase();
        bucket = new ArrayList<>();
        String[] queryWords = query.toLowerCase().split(" ");
        //finding the smallest word
        String smallest = queryWords[0];
        for(String word : queryWords){
            smallest = smallest.length() > word.length() ? word : smallest;
        }
        //making the bucket based on the length of the words, in comparison to the smallest one.
        bucket.add(smallest);
        for(String word : queryWords){
            for(int i = 0 ; i < word.length() - smallest.length(); i++ ){
                bucket.add(word);
            }
        }
        //picking up a random number from 0 to size of the bucket without replacement i.e. same word cannot be chosen again.
        //doing this till we 40% words of the query.
        int queryLength = queryWords.length;
        System.out.println("Bucket is =" + bucket);
        for(int i = 0; i < 0.4 * queryLength; i++ ){
            int max = bucket.size() - 1, min = 0;
            int index = (int) ((Math.random() * ((max - min) + 1)) + min);
            String chosenWord = bucket.get(index);
            //now removing this word from the bucket so that it's not chosen again.
            while (bucket.contains(chosenWord)){
                bucket.remove(chosenWord);
            }
            System.out.println("Chosen Word = " + chosenWord);
            query = query.replaceAll(chosenWord,"");
            System.out.println(query);
            //shuffling the letters of the chosen word.
            String toReplace = shuffle(chosenWord).get(chosenWord);
            tempQuery = tempQuery.replace(chosenWord, toReplace);
            }
        return tempQuery;
            
    }
    //citation -> https://www.quickprogrammingtips.com/java/how-to-shuffle-a-word-in-java.html
    /**
     * Shuffles a given word. Randomly swaps characters 10 times.
     * @param word
     * @return
     */
    private Map<String, String> shuffle(String word) {
       	Map shuffleMap = new HashMap<>();
        if(word.length() < 4) {
             shuffleMap.put(word, word);
             return shuffleMap;
        }
        	String shuffledWord = word; // start with original
        int wordSize = word.length();
        int shuffleCount = 5; // let us randomly shuffle letters 10 times
        for(int i=0;i<shuffleCount;i++) {
            //swap letters in two indexes
            int position1 = ThreadLocalRandom.current().nextInt(1, wordSize - 1);
            int position2 = ThreadLocalRandom.current().nextInt(1, wordSize - 1);
            shuffledWord = swapCharacters(shuffledWord,position1,position2);
        }
        if(shuffledWord.equals(word)) {
            shuffledWord = shuffle(word).get(word);
        }
        System.out.println(shuffledWord);
        shuffleMap.put(word, shuffledWord);
        return shuffleMap;
    }

    /**
     * Swaps characters in a string using the given character positions
     * @param shuffledWord
     * @param position1
     * @param position2
     * @return
     */
    private String swapCharacters(String shuffledWord, int position1, int position2) {
        char[] charArray = shuffledWord.toCharArray();
        char temp = charArray[position1];
        charArray[position1] = charArray[position2];
        charArray[position2] = temp;
        return new String(charArray);
    }
    public static void main(String[] args){
        ECTask1 e = new ECTask1();
        System.out.println(e.makeBucket(" I am interested in articles written either by Prieve or Udo Pooch"));
        //Result:  i am ineseerttd in alitcres wteitrn either by pervie or udo pocoh, here 40% of the words are selected and their letters are shuffled 
    }
}
