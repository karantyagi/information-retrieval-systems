package com.ir.project.retrievalmodel;

import java.io.IOException;
import java.util.Set;

public interface RetrievalModel {

    /**
     * @return List of retrieved documents after running Lucene retrieval model on a given query
     */
    Set<SearchedDocument> search(String query) throws IOException;

}
