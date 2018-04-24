package com.ir.project.retrievalmodel;

import com.ir.project.utils.SearchQuery;

import javax.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

public class RetrievalTask implements Callable<List<RetrievedDocument>>{

    private RetrievalModel retrievalModel;
    private SearchQuery query;
    private String outFolder;
    private String systemName; // represents the RUN e.g. NoStopNoStem
    private String literal;


    public RetrievalTask(@NotNull RetrievalModel retrievalModel, @NotNull SearchQuery query,
                         @NotNull String outFileName, @NotNull String systemName) {
        this.retrievalModel = retrievalModel;
        this.query = query;
        this.systemName =  systemName;
        this.outFolder = outFileName+ retrievalModel.getModelType().name()+ File.separator +systemName+File.separator;
        this.literal = "Q0";
    }

    @Override
    public List<RetrievedDocument> call() throws Exception {
        List<RetrievedDocument> retrievedDocumentList = retrievalModel.search(this.query);
        writeToFile(retrievedDocumentList);
        return retrievedDocumentList;
    }

    private void writeToFile(List<RetrievedDocument> retrievedDocumentList) throws IOException {
        //TODO:

        // Create 'retrievedResults' directory

        if (!new File(outFolder).isDirectory())
        {
            File dir = new File(outFolder);
            dir.mkdirs();
            System.out.println(outFolder+" created!");
        }

        // Writing top 100 results
        FileWriter fw = new FileWriter(outFolder+"QUERY_" + query.getQueryID()+"_results.txt");
        BufferedWriter bw= new BufferedWriter(fw);
        RetrievedDocument rd;
        for (int i = 0; i < retrievedDocumentList.size(); i++){
            rd = retrievedDocumentList.get(i);
            rd.getDocumentID();
            rd.getScore();
            if(i<100){

                bw.append(this.query.getQueryID()+"\t"+this.literal+"\t"+rd.getDocumentID()
                        +"\t"+(i+1)+"\t"+rd.getScore()+"\t\t"+retrievalModel.getModelType().name()
                        +this.systemName);

                if(i<99){
                    bw.append("\n");
                }
                /*

                System.out.println(this.query.getQueryID()+"\t"+this.literal+"\t"+rd.getDocumentID()
                        +"\t"+(i+1)+"\t"+rd.getScore()+"\t\t"+retrievalModel.getModelType().name()
                        +this.systemName+"\r");

                System.out.printf("%-4d %4s %-80s  %-4d   %3.7f  %s%s \n",
                        this.query.getQueryID(),this.literal,rd.getDocumentID(),rd.getScore(),
                        retrievalModel.getModelType().name(),this.systemName);
                */
            }
        }
        bw.close();
        fw.close();
        System.out.println("\""+retrievalModel.getModelType().name()+"\\"+this.systemName+"\\QUERY_" + query.getQueryID()+"_results.txt\" created!");
    }
}
