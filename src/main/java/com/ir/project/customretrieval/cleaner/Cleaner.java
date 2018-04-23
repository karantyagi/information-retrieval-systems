package com.ir.project.customretrieval.cleaner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ir.utils.Constants;

public class Cleaner {

    public static final int MAX_THREADS = 100;

    public static void main(String args[]) {
        String documentFolderPath =Constants.CORPUSPATH;
        String outputFolderPath = Constants.CLEANED_OUTPUT_FOLDER_PATH;
        List<String> cleanedFiles = cleanDocuments(documentFolderPath, outputFolderPath);

        System.out.println("Cleaned " + cleanedFiles.size() + " files.");
    }

    public static List<String> cleanDocuments(String documentFolderPath, String outputFolderPath) {
        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.isDirectory()) {
            System.out.println("Error!! A file with output folder path name exists!");
            return new ArrayList<String>();
        } else if (!outputFolder.exists()) {
            System.out.println("Error!! A folder doesn't exist!");
            return new ArrayList<String>();
        }
        List<String> cleanedFiles = new ArrayList<String>();
        File documentFolder = new File(documentFolderPath);

        if (documentFolder.isDirectory()) {

            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
            List<Future<String>> futures = new ArrayList<Future<String>>();
            for (File file : documentFolder.listFiles()) {

                if (file.isFile() && file.getName().endsWith(".html")) {
                    CleanerThread thread =
                            new CleanerThread(file.getAbsolutePath(),
                                    outputFolderPath + "/" + file.getName() + "_cleaned");
                    Future<String> f = executor.submit(thread);
                    futures.add(f);
                }
            }

            executor.shutdown();
            cleanedFiles = pollForCompletion(futures);
        }

        return cleanedFiles;

    }
    private static List<String> pollForCompletion(List<Future<String>> futures) {
        List<String> cleanedFiles = new ArrayList<String>();
        for (Future<String> f : futures) {
            try {
                String outFile = f.get();
                cleanedFiles.add(outFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return cleanedFiles;
    }
}
