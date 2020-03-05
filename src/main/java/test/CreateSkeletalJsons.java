package test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CreateSkeletalJsons {
    public void writeJsonFile(String fileName) throws IOException, ParseException {
        JSONObject obj = (JSONObject) new JSONParser().parse(new FileReader("/Users/loaner/Desktop/Comparator/src/main/resources/skeletal.json"));
        try {

            FileWriter file = new FileWriter("/Users/loaner/Desktop/Comparator/src/main/resources/ExpectedResponses/" + fileName + ".json");
            file.write(obj.toJSONString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createJsonFiles() {
        for (String fileName : Main.fileNames) {
            try {
                writeJsonFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }
}
