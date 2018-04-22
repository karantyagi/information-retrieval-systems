package com.ir.project.retrievalmodel;

public class SearchedDocument implements Document {


    private String documentID;
    private double score;
    private int rank;

    /**
     * @param documentID
     */
    public SearchedDocument( String documentID) {

        this.documentID = documentID;
        this.rank = 0;
        this.score = 0.0;
    }

    /**
     * @return document ID
     */
    public String docID() {
        return this.documentID;
    }

    /**
     * @return score of document
     */
    public double score() {
        return this.score;
    }

    public void updateScore(double score) {
        this.score = score;

    }

    /**
     * @return rank of document as per its score
     */
    public int rank() {
        return this.rank;
    }

    public void updateRank(int r) {
        this.rank =r;

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
