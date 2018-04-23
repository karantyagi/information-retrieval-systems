package com.ir.project.cleaner;

import com.sun.istack.internal.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CleanerThread implements Callable<String> {

    private String inputFilePath;
    private String outPutFilePath;


    private CleanerThread() {

    }

    public CleanerThread(@NotNull String inputFilePath, @NotNull String outPutFilePath) {
        this.inputFilePath = inputFilePath;
        this.outPutFilePath = outPutFilePath;
    }

    public String call() throws Exception {
        File fileToClean = new File(inputFilePath);
        Document doc = Jsoup.parse(fileToClean, "UTF-8");
        Elements elements = doc.body().getElementsByTag("pre");
        StringBuffer cleanedText = new StringBuffer();

        for(Element e: elements) {
            for (String line : e.text().split("\n")) {
                if (line.length() == 0)
                    continue;

                if (isIgnorableLine(line))
                    continue;

                cleanedText.append(line).append("\n");
            }
        }


        writeToFile(cleanedText.toString());

        return outPutFilePath;
    }

    // if line contains three columns and all of them are digits.
    private boolean isIgnorableLine(String line) {
        String [] lineTokens = line.split("\t");
        if (lineTokens.length != 3)
            return false;

        boolean allNumbers = true;

        for (String token : lineTokens) {
            allNumbers &= token.matches("-?\\d+(\\.\\d+)?");
        }

        return allNumbers;
    }


    private void writeToFile(String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.outPutFilePath));
        writer.write(content);
        writer.close();
    }
}
