package com.ir.project.retrievalmodel;

public class RetrievedDocument {

    private String documentID;
    private double score;
    private int rank;

    private RetrievedDocument(){}

    public RetrievedDocument(String documentID) {

        this.documentID = documentID;
        this.rank = 0;
        this.score = 0.0;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String result = "DocID: "+ documentID+" Rank: "+ String.valueOf(rank)+" Score: "+String.valueOf(score);
        return result.trim();
    }




}
