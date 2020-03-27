package TEST_HARNESS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import com.google.common.collect.Sets;

public final class Util {
    private Util() {
        throw new AssertionError("No instances for you!");
    }

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet().stream().flatMap(Util::flatten)
                  .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {

        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            Map<?, ?> properties = (Map<?, ?>) entry.getValue();
            return properties.entrySet().stream().flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "." + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size()).mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "." + i, list.get(i)))
                            .flatMap(Util::flatten);
        }

        return Stream.of(entry);
    }

    public static List<String> getCommonFileNames(String folderLoc1, String folderLoc2) {
        Set<String> expectedOutputFiles = getNames(folderLoc1).stream().collect(Collectors.toSet());
        Set<String> stageFiles = getNames(folderLoc2).stream().collect(Collectors.toSet());
        return Sets.intersection(expectedOutputFiles, stageFiles).stream().collect(Collectors.toList());
    }

    public static List<String> getNames(String folderName) {
        List<String> stringList = new ArrayList<>();
        File folder = new File(getPropertyFromFile("application.properties").getProperty(folderName));
        File[] listOfFiles = folder.listFiles();
        //fileNames = new ArrayList<String>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().split("\\.").length == 2) {
                    String fileName = listOfFiles[i].getName().split("\\.")[0];
                    if (fileName.length() != 0) {
                        stringList.add(fileName);
                    }
                }
            }
        }
        return stringList;
    }

    public static Properties getPropertyFromFile(final String fileName) {
        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = new FileInputStream(fileName);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
        return properties;
    }

    public static String replaceNumbers(String str1) {
        String output = str1.replaceAll("(.+?\\.)(\\d{1,})(\\..+?)", "$1n$3");
        return output;
    }

}
