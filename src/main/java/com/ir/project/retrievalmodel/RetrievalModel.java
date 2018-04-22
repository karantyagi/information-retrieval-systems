package com.ir.project.retrievalmodel;

import java.util.Set;

public interface RetrievalModel {

    /**
     * @return List of retrieved documents after running Lucene retrieval model on a given query
     */
    Set<String> search(String query);

}
