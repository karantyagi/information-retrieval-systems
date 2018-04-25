package com.ir.project.evaluation;

import com.ir.project.retrievalmodel.RetrievedDocument;
import com.ir.project.utils.SearchQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {

    private String systemName;
    private Map<Integer, List<String>> queryRelevantDocList;
    private Map<SearchQuery, List<RetrievedDocument>> queryListMap;

    public Evaluator(String systemName,
                     Map<Integer, List<String>> queryRelevantDocList,
                     Map<SearchQuery, List<RetrievedDocument>> queryListMap) {
        this.systemName = systemName;
        this.queryRelevantDocList = queryRelevantDocList;
        this.queryListMap = queryListMap;
    }


    public EvaluationStats evaluate() {
        Map<SearchQuery, PrecisionRecallTable> precisionRecallTableAndQueryMap =
                new HashMap<>();

        for (Map.Entry<SearchQuery, List<RetrievedDocument>> entry :
                queryListMap.entrySet()) {
            PrecisionRecallTable table = generatePrecisionAndRecallTable(entry.getKey(), entry.getValue());
            precisionRecallTableAndQueryMap.put(entry.getKey(), table);
        }

        double MAP = calculateMAP(precisionRecallTableAndQueryMap);
        double MRR = calculateMRR(precisionRecallTableAndQueryMap);

        return new EvaluationStats(systemName, MAP, MRR, precisionRecallTableAndQueryMap);
    }

    private double calculateMRR(Map<SearchQuery, PrecisionRecallTable> precisionRecallTableAndQueryMap) {
        double sumOfReciprocalRanks = 0.0D;
        for (Map.Entry<SearchQuery, PrecisionRecallTable> entry : precisionRecallTableAndQueryMap.entrySet()) {
            sumOfReciprocalRanks += entry.getValue().getReciprocalRank();
        }

        return sumOfReciprocalRanks/precisionRecallTableAndQueryMap.keySet().size();
    }


    private double calculateMAP(Map<SearchQuery, PrecisionRecallTable> precisionRecallTableAndQueryMap) {
        double sumOfAvgPrecisions = 0.0D;
        for (Map.Entry<SearchQuery, PrecisionRecallTable> entry : precisionRecallTableAndQueryMap.entrySet()) {
            if (!entry.getValue().getAveragePrecision().isNaN()) {
                sumOfAvgPrecisions += entry.getValue().getAveragePrecision();
            }

        }

        return sumOfAvgPrecisions/precisionRecallTableAndQueryMap.keySet().size();
    }

    private PrecisionRecallTable generatePrecisionAndRecallTable(SearchQuery query,
                                                                 List<RetrievedDocument> documentList) {

        int totalRelevantDocsSoFar = 0;
        List<PrecisionAndRecall> precisionAndRecalls = new ArrayList<>(); // array list maintains the order.
        for (int i = 0; i < documentList.size(); i++) {
            RetrievedDocument doc = documentList.get(i);
            boolean isRelevant = false;

            if (null != this.queryRelevantDocList.get(query.getQueryID()) &&
                    this.queryRelevantDocList.get(query.getQueryID()).contains(doc.getDocumentID())) {
                totalRelevantDocsSoFar++;
                isRelevant = true;
            }

            double precision = ((double)totalRelevantDocsSoFar) / (i+1);
            double recall = 0;
            if (null != queryRelevantDocList.get(query.getQueryID())) {
                recall = ((double)totalRelevantDocsSoFar)/ queryRelevantDocList.get(query.getQueryID()).size();
            }

            PrecisionAndRecall precisionAndRecall =
                    new PrecisionAndRecall(doc.getDocumentID(), precision, recall, isRelevant);
            precisionAndRecalls.add(precisionAndRecall);
        }

        return new PrecisionRecallTable((precisionAndRecalls));
    }

}
