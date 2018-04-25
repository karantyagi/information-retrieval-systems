package com.ir.project.evaluation;

import java.util.List;

public class PrecisionRecallTable {
    List<PrecisionAndRecall> precisionRecallList;
    private Double averagePrecision;

    private PrecisionRecallTable() {

    }

    public PrecisionRecallTable(List<PrecisionAndRecall> precisionRecallList) {
        this.precisionRecallList = precisionRecallList;
        calculateAveragePrecision();
    }

    private void calculateAveragePrecision() {
        int totalRelevantDocs = 0;
        double totalPrecision = 0.0D;
        for (PrecisionAndRecall precisionAndRecall : precisionRecallList) {
            if (precisionAndRecall.isRelevant()) {
                totalPrecision+=precisionAndRecall.getPrecision();
                totalRelevantDocs++;
            }
        }
        this.averagePrecision = totalPrecision/totalRelevantDocs;
    }

    public List<PrecisionAndRecall> getPrecisionRecallList() {
        return precisionRecallList;
    }

    private void setPrecisionRecallList(List<PrecisionAndRecall> precisionRecallList) {
        this.precisionRecallList = precisionRecallList;
    }

    public Double getPrecisionAtK(int k) {
        return this.precisionRecallList.get(k).getPrecision();
    }

    public Double getReciprocalRank() {
        if (null == precisionRecallList || precisionRecallList.isEmpty()){
            return 0.0D;
        }

        for (int i = 0; i < precisionRecallList.size(); i++) {
            if (precisionRecallList.get(i).isRelevant()) {
                return 1.0D/(i+1);
            }
        }

        return 0.0D;
    }

    public Double getAveragePrecision() {
        return averagePrecision;
    }

    private void setAveragePrecision(Double averagePrecision) {
        this.averagePrecision = averagePrecision;
    }

    @Override
    public String toString() {
        StringBuffer table = new StringBuffer();
        table.append("---------------------------------------------");
        table.append(System.getProperty("line.separator"));

        for (PrecisionAndRecall precisionAndRecall : precisionRecallList) {
            table.append(precisionAndRecall.toString())
                    .append(System.getProperty("line.separator"));
        }
        table.append("---------------------------------------------");
        table.append(System.getProperty("line.separator"));
        table.append("Precision at K = 5 :")
                .append(getPrecisionAtK(5))
                .append(System.getProperty("line.separator"));
        table.append("Precision at K = 20 :")
                .append(getPrecisionAtK(20))
                .append(System.getProperty("line.separator"));
        table.append("---------------------------------------------");
        table.append(System.getProperty("line.separator"));
        return table.toString();
    }
}
