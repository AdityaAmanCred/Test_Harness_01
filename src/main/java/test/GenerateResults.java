package test;

import java.io.FileWriter;
import java.io.IOException;

public class GenerateResults {
    public void writeTofile(String results) {
        //Write JSON file
        try (FileWriter file = new FileWriter("/Users/loaner/Desktop/Comparator/src/main/resources/Results/results.json")) {
            file.write(results);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
