package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static List<String> fileNames = new ArrayList<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> getNames() {
        List<String> stringList = new ArrayList<>();
        File folder = new File("/Users/loaner/Desktop/Comparator/src/main/resources/PDFs");
        File[] listOfFiles = folder.listFiles();
        //fileNames = new ArrayList<String>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                // System.out.println("File " + listOfFiles[i].getName());
                if (listOfFiles[i].getName().split("\\.").length == 2) {
                    String fileName = listOfFiles[i].getName().split("\\.")[0];
                    stringList.add(fileName);
                }
            }
        }
        return stringList;
    }

    public static void main(String[] args) throws IOException, ParseException {
        fileNames = getNames();
        CreateSkeletalJsons createSkeletalJsons = new CreateSkeletalJsons();
        createSkeletalJsons.createJsonFiles();
        FetchStageResponses fetchStageResponses = new FetchStageResponses("sc_lf4", "/Users/loaner/Desktop/Comparator/src/main/resources/PDFs");
        fetchStageResponses.fetchAll();

        Comparator comparator = new Comparator();
        JSONArray results = comparator.compareAll();
        GenerateResults gR = new GenerateResults();
        gR.writeTofile(mapper.writeValueAsString(results));
    }
}
