package com.ir.project.retrievalmodel;

import java.io.IOException;
import java.util.List;

public interface RetrievalModel {

    /**
     * @return Sorted List of retrieved documents for a given query
     */
    List<RetrievedDocument> search(String query) throws IOException;

}
