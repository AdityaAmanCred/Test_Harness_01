package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.getNames;
import java.util.List;
import lombok.Getter;

@Getter
public abstract class ParserResponses extends Responses {
    protected List<String> fileNames;

    protected String primaryParserTemplateId;

    protected String secondaryParserTemplateId;

    protected String pdfLocation;

    protected String envName;

    public ParserResponses(double fetchRate) {
        super(fetchRate);
        this.fileNames = getNames("PDF_DOWNLOAD_LOC");
        this.primaryParserTemplateId = fetchProperty("PRIMARY_PARSER_TEMPLATE_ID");
        this.secondaryParserTemplateId = fetchProperty("SECONDARY_PARSER_TEMPLATE_ID");
        this.pdfLocation = fetchProperty("PDF_DOWNLOAD_LOC");
    }

    public abstract void fetchResponse(String fileName, Environment env);

    public abstract void fetchAllResponses(Environment env);

}
