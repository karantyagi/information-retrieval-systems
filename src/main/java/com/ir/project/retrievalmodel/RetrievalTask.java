package com.ir.project.retrievalmodel;

import com.ir.project.utils.SearchQuery;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.Callable;

public class RetrievalTask implements Callable<List<RetrievedDocument>>{

    private RetrievalModel retrievalModel;
    private SearchQuery query;
    private String outFolder;

    public RetrievalTask(@NotNull RetrievalModel retrievalModel, @NotNull SearchQuery query,
                         @NotNull String outFileName) {
        this.retrievalModel = retrievalModel;
        this.query = query;
    }


    @Override
    public List<RetrievedDocument> call() throws Exception {
        List<RetrievedDocument> retrievedDocumentList = retrievalModel.search(this.query);
        writeToFile(retrievedDocumentList);
        return retrievedDocumentList;
    }

    private void writeToFile(List<RetrievedDocument> retrievedDocumentList) {
        //TODO:
    }
}
