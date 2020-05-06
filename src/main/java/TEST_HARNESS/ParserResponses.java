package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import java.util.List;

public abstract class ParserResponses extends Responses {
    protected List<String> fileNames;

    protected String stageId;

    protected String prodId;

    protected String pdfLocation;

    protected String envName;

    public ParserResponses(double fetchRate) {
        super(fetchRate);
        this.fileNames = getNames("PDF_DOWNLOAD_LOC");
        this.stageId = fetchProperty("STAGE_ID");
        this.prodId = fetchProperty("PROD_ID");
        this.pdfLocation = fetchProperty("PDF_DOWNLOAD_LOC");
    }

    public abstract void fetchResponse(String fileName, Environment env);

    public abstract void fetchAllResponses(Environment env);

}
