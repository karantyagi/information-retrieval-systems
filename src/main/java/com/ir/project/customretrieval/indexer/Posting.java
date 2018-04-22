package com.ir.project.customretrieval.indexer;

public class Posting {
    private String documentId;
    private int frequency;

    private Posting(){
    }

    public Posting(String documentId, int frequency) {
        this.documentId = documentId;
        this.frequency = frequency;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
