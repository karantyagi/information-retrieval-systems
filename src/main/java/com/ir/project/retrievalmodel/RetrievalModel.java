package com.ir.project.retrievalmodel;

import com.ir.project.utils.SearchQuery;

import java.io.IOException;
import java.util.List;

public interface RetrievalModel {

    RetrievalModelType getModelType();

    List<RetrievedDocument> search(SearchQuery query) throws IOException;

}
