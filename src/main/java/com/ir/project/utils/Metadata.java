package com.ir.project.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Metadata {
    private static String path="";
    public static int fileCount=0;

    private Metadata() {
    }
    /**
     * @throws IOException
     */
    public static double averageDocLen() throws IOException{

        File directory = new File(path);
        File[] fList = directory.listFiles();
        double totalDocLen = 0.0d;
        for (File file : fList){
            if(!file.getName().endsWith(".txt")) continue;
            fileCount++;
            BufferedReader br = new BufferedReader(new FileReader(file));
            totalDocLen += br.toString().length();
        }
        return totalDocLen/fileCount ;
    }
    /**
     *
     * @return total number of documents
     */
    public static int totalDocs(){
        File directory = new File(path);
        File[] fList = directory.listFiles();
        for (File file : fList){
            if(!file.getName().endsWith(".txt")) continue;
            fileCount++;
        }
        return fileCount;


    }



}
