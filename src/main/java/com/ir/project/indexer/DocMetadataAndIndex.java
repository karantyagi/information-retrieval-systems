package com.ir.project.indexer;

import java.util.List;
import java.util.Map;

public class DocMetadataAndIndex {
    private  Map<String,List<Posting>> index;
    private  Map<String, Integer> documentLength;

    private DocMetadataAndIndex() {
    }

    public DocMetadataAndIndex(Map<String, List<Posting>> index, Map<String, Integer> documentLength) {
        this.index = index;
        this.documentLength = documentLength;
    }

    public Map<String, List<Posting>> getIndex() {
        return index;
    }

    public void setIndex(Map<String, List<Posting>> index) {
        this.index = index;
    }

    public Map<String, Integer> getDocumentLength() {
        return documentLength;
    }

    public void setDocumentLength(Map<String, Integer> documentLength) {
        this.documentLength = documentLength;
    }
}
