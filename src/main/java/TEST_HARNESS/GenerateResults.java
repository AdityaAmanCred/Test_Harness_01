package TEST_HARNESS;

import static TEST_HARNESS.Util.getPropertyFromFile;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateResults {
    public void writeTofile(String fileName, String results) {
        //Write JSON file
        try (FileWriter file = new FileWriter(getPropertyFromFile("application.properties").getProperty("RESULT_DIR") + fileName + ".json")) {
            file.write(results);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
