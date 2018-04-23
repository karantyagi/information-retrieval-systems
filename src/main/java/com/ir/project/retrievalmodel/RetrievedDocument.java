package com.ir.project.retrievalmodel;

public class RetrievedDocument {

    private String documentID;
    private double score;

    private RetrievedDocument(){}

    public RetrievedDocument(String documentID) {

        this.documentID = documentID;
        this.score = 0;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String result = "DocID: "+ documentID+" Score: "+String.valueOf(score);
        return result.trim();
    }




}
