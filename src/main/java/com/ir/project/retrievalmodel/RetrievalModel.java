package com.ir.project.retrievalmodel;

import java.io.IOException;
import java.util.List;

public interface RetrievalModel {

    List<RetrievedDocument> search(String query) throws IOException;

}
