package com.ir.project.retrievalmodel;

import java.io.IOException;
import java.util.Set;

public interface RetrievalModel {

    /**
     * @return List of retrieved documents for a  given query
     */
    Set<RetrievedDocument> search(String query) throws IOException;

}
