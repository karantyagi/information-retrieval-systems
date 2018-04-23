package com.ir.project.utils;

import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static String processedWord(String word) {
        // remove all punctuations
        return word.replaceAll("[^\\p{ASCII}]", "")
                //.replaceAll("(?<![0-9a-zA-Z])[\\p{Punct}]", "")
                .replaceAll("(?<![0-9])[^\\P{P}-](?![0-9])", "") // retain hyphens in text
                .replaceAll("[\\p{Punct}](?![0-9a-zA-Z])", "")
                .replace("(", "")
                .replace(")", "")
                .replaceAll("/"," ");
    }

    public static String processedText(String line) {

        return (line.trim().toLowerCase()
                .replaceAll("\\("," ")
                .replaceAll("\\)"," "));
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

        final String WHITESPACE = "\\s"; // any whitespace character -  [ \t\n\x0B\f\r]+
        final String MULTIPLE_WHITESPACES = "//s"; // ????????????? mutliple whitespaces - regex


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
}
