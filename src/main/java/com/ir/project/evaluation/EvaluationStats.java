package com.ir.project.evaluation;

import com.ir.project.utils.SearchQuery;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class EvaluationStats {

    private String runModel; // just for info.
    private Double map; // mean average precision;
    private Double mrr;
    Map<SearchQuery, PrecisionRecallTable> precisionRecallTableAndQueryMap;

    private EvaluationStats() {
    }

    public EvaluationStats(@NotNull String runModel, @NotNull Double map,
                           @NotNull Double mrr,
                           @NotNull Map<SearchQuery, PrecisionRecallTable> precisionRecallTableAndQueryMap) {
        this.runModel = runModel;
        this.map = map;
        this.mrr = mrr;
        this.precisionRecallTableAndQueryMap = precisionRecallTableAndQueryMap;
    }

    public String getRunModel() {
        return runModel;
    }

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    public Double getMap() {
        return map;
    }

    private void setMap(Double map) {
        this.map = map;
    }

    public Double getMrr() {
        return mrr;
    }

    private void setMrr(Double mrr) {
        this.mrr = mrr;
    }

    public Map<SearchQuery, PrecisionRecallTable> getPrecisionRecallTableAndQueryMap() {
        return precisionRecallTableAndQueryMap;
    }

    private void setPrecisionRecallTableAndQueryMap(Map<SearchQuery, PrecisionRecallTable> precisionRecallTableAndQueryMap) {
        this.precisionRecallTableAndQueryMap = precisionRecallTableAndQueryMap;
    }

    public void writePrecisionTablesToFolder(String outputFolderPath) {

        if (outputFolderPath.charAt(outputFolderPath.length() - 1 ) != File.separator.charAt(0))
            outputFolderPath = outputFolderPath + "/";

        if (!new File(outputFolderPath).exists()) {
            new File(outputFolderPath).mkdirs();
        }
        for (Map.Entry<SearchQuery, PrecisionRecallTable> entry : precisionRecallTableAndQueryMap.entrySet()) {
            String outFilePath = outputFolderPath + entry.getKey().getQueryID() + this.runModel + ".table";
            StringBuffer content = new StringBuffer();
            content
                    .append("Query ID: ")
                    .append(entry.getKey().getQueryID())
                    .append(System.getProperty("line.separator"))
                    .append("Query : ")
                    .append(entry.getKey().getQuery())
                    .append(System.getProperty("line.separator"))
                    .append(entry.getValue().toString());

            try {
                Files.write(Paths.get(outFilePath), content.toString().getBytes());
            } catch (IOException e) {
                 e.printStackTrace();
            }
         }
    }

}
