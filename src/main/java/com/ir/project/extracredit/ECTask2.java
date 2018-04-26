package com.ir.project.extracredit;
import com.ir.project.indexer.DocMetadataAndIndex;
import com.ir.project.indexer.Posting;
import com.ir.project.utils.SearchQuery;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ECTask2 {
   private static  Map<String, List<Posting>> index;
   public ECTask2(DocMetadataAndIndex metadataAndIndex){
      this.index = metadataAndIndex.getIndex();
   }
   public static List<SearchQuery> getMitigatedQuaries(List<SearchQuery>queryFile) throws IOException {
      List<SearchQuery> l11 = ECTask1.returnNoisySearchQuery(queryFile);
      for (SearchQuery sQuery : l11) {
         String updatedQuery = mitigate(sQuery.getQuery());
         sQuery.setQuery(updatedQuery);
      }

      return l11;
   }

   public static  String mitigate(String query){
      String[] temp = query.split(" ");
      for(String word : temp){
         if(word.equals("")) continue;
         if(!index.containsKey(word)){
            String match = word;
            int minD = Integer.MAX_VALUE;
            for(String key : index.keySet()){
               if(key.equals("")) continue;
               if(Math.abs(key.length() - word.length()) <= 2 && key.charAt(0) == word.charAt(0) && key.charAt(key.length() - 1 ) == word.charAt(word.length() - 1)){
                  int distance = minDistance(key, word);
                  if(distance < minD){
                     minD = distance;
                     match = key;
                  }
               }
            }
            query = query.replace(word, match);
         }
      }
      return query;
   }
   public static  int minDistance(String word1, String word2) {
      int[][] c = new int[word1.length()+1][word2.length()+1];
      char[] a = word1.toCharArray();
      char[] b = word2.toCharArray();

      for(int i = 0; i<c.length ; i++){
         for(int j=0; j<c[0].length; j++){
            if(i==0) {
               c[i][j] = j;

               continue;
            };
            if(j==0) {
               c[i][j] = i;

               continue;
            };

            int same=0;
            if(a[i-1]==b[j-1]) {
               same= c[i-1][j-1];

            }
            else same = 1+ c[i-1][j-1];
            //insert
            int insert = 1+c[i][j-1];
            //delete
            int delete =1+ c[i-1][j];

            c[i][j] = Math.min(insert,Math.min(same,delete));
         }
      }
      return c[word1.length()][word2.length()];
   }

}