package com.ir.project.utils;

public class SearchQuery {

    private int queryID;
    private String query;

    public SearchQuery(int queryID,String query){
        this.query = query;
        this.queryID = queryID;
    }

    public int getQueryID() {
        return queryID;
    }

    public void setQueryID(int queryID) {
        this.queryID = queryID;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "Query1{" +
                "queryID=" + queryID +
                ", query='" + query + '\'' +
                '}';
    }
}
