package com.ir.project.stemmer;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.Map;
import java.util.Set;

public class QueryEnhancer {
    private Map<String, Set<String>> stemClasses;
    private PorterStemmer porterStemmer;

    public QueryEnhancer(Map<String, Set<String>> stemClasses) {
        this.stemClasses = stemClasses;
        this.porterStemmer = new PorterStemmer();
    }

    private QueryEnhancer() {
    }


    public String enhanceQuery(String query) {
        StringBuffer enhancedQuery = new StringBuffer();

        for (String queryWord : query.split(" ")) {
            enhancedQuery.append(queryWord)
                    .append(" ");

            porterStemmer.setCurrent(queryWord);
            porterStemmer.stem();
            String stemClass = porterStemmer.getCurrent();

            Set<String> stemClassWords = stemClasses.get(stemClass);

            if (null != stemClassWords && !stemClassWords.isEmpty()) {
                for (String stemWord : stemClassWords) {
                    enhancedQuery.append(stemWord).append(" ");
                }
            }
        }

        return enhancedQuery.toString().trim();
    }
}
