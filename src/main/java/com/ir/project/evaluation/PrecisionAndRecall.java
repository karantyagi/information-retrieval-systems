package com.ir.project.evaluation;

public class PrecisionAndRecall {
    private String docId;
    private Double precision;
    private Double recall;
    private boolean isRelevant;

    private PrecisionAndRecall() {

    }

    public PrecisionAndRecall(String docId, Double precision,
                              Double recall, boolean isRelevant) {
        this.docId = docId;
        this.precision = precision;
        this.recall = recall;
        this.isRelevant = isRelevant;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public boolean isRelevant() {
        return isRelevant;
    }

    public void setRelevant(boolean relevant) {
        isRelevant = relevant;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public String toString() {
        StringBuffer tableEntry = new StringBuffer();
        tableEntry
                .append("| ")
                .append(docId)
                .append(" | \t | ")
                .append(precision)
                .append(" | \t | ")
                .append(recall)
                .append(" | \t | ");
        String out = this.isRelevant? tableEntry.append("R |").toString() : tableEntry.append("NR |").toString();
        return out;
    }
}
