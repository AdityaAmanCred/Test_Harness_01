package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Getter;

@Getter
public abstract class ParserResponses extends Responses implements Runnable {
    //    protected Set<String> fileNames;
    //
    //    protected Set<String> fetchedFileNamesPrimaryParser;
    protected String fileName;

    protected Environment environment;

    protected String primaryParserTemplateId;

    protected String secondaryParserTemplateId;

    protected String pdfLocation;

    protected String envName;

    protected ParserName parserName;

    public ParserResponses(String fileName, Environment environment) {
        //super(fetchRate);
        //        this.fileNames = getNames("PDF_DOWNLOAD_LOC").stream().collect(Collectors.toSet());
        //        fetchedFileNamesPrimaryParser = new HashSet<>();
        this.fileName = fileName;
        this.environment = environment;
        this.primaryParserTemplateId = fetchProperty("PRIMARY_PARSER_TEMPLATE_ID");
        this.secondaryParserTemplateId = fetchProperty("SECONDARY_PARSER_TEMPLATE_ID");
        this.pdfLocation = fetchProperty("PDF_DOWNLOAD_LOC");
        //        parserName = null;
    }

    public abstract void fetchResponse();
    //    public void fetchAllResponses(Environment env) {
    //        fetchedFileNamesPrimaryParser = new HashSet<>(getNames(this.getParserType().toString().equalsIgnoreCase("PRIMARY") ? "PRIMARY_DIR" : "SECONDARY_DIR"));
    //        this.primaryParserFetchCounter = fetchedFileNamesPrimaryParser.size();
    //        fileNames.removeAll(this.fetchedFileNamesPrimaryParser);
    //        if (fileNames.size() == 0) {
    //            System.out.println(String.format("All PDFs files already transformed for %s", this.parserName.name()));
    //        }
    //        while (this.retryAttemptsLeft-- > 0 && fileNames.size() > 0) {
    //            if (this.retryAttemptsLeft != this.maxretryAttempts - 1) {
    //                System.out.println(String.format("Retrying to fetch %s responses for failed PDFs", this.parserName.name()));
    //            }
    //            Iterator<String> iterator = fileNames.iterator();
    //            while (iterator.hasNext()) {
    //                rateLimiter.acquire(1);
    //                String fileName = iterator.next();
    //                if (!this.fetchedFileNamesPrimaryParser.contains(fileName)) {
    //                    if (this.fetchResponse(fileName + ".pdf", env)) {
    //                        iterator.remove();
    //                        this.fetchedFileNamesPrimaryParser.add(fileName);
    //                    }
    //                } else {
    //                    System.out
    //                            .println(String.format("%s Parser response for file " + fileName + " already fetched", this.getParserType().toString()));
    //                }
    //            }
    //        }
    //    }

//    public <T> getInstance(Class<T extends ParserResponses> subclass)
//            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        return subclass.getDeclaredConstructor().newInstance(fileName,environment);
//    }
}
