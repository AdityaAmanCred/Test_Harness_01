package TEST_HARNESS.parse;

import static TEST_HARNESS.utils.Util.fetchProperty;
import static TEST_HARNESS.utils.Util.getParserNameForParserType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
@Data
@Slf4j
public abstract class Parser implements Runnable {
    protected ParserType parserType;

    protected ParserName parserName;

    protected Environment parserEnvironment;

    protected String templateId;

    protected String envName;

    protected String pdfLocation;

    protected String fileName;

    public Parser(ParserType parserType, String fileName) {
        this.parserType = parserType;
        this.fileName = fileName;
        this.parserName = getParserNameForParserType(this.parserType);
        setParserEnvironment();
        setEnvName();
        setTemplateId();
        this.pdfLocation = fetchProperty("PDF_DOWNLOAD_LOC");
    }

    private void setEnvName() {
        if (parserEnvironment == Environment.PROD) {
            this.envName = "prod";
        } else {
            envName = "stg";
        }
    }

    private void setParserEnvironment() {
        if (parserType == ParserType.PRIMARY) {
            if (fetchProperty("PRIMARY_PARSER_ENV").equalsIgnoreCase("STAGE")) {
                parserEnvironment = Environment.STAGE;
            } else {
                parserEnvironment = Environment.PROD;
            }

        } else {
            if (fetchProperty("SECONDARY_PARSER_ENV").equalsIgnoreCase("STAGE")) {
                parserEnvironment = Environment.STAGE;
            } else {
                parserEnvironment = Environment.PROD;
            }
        }
    }

    private void setTemplateId() {
        if (parserType == ParserType.PRIMARY) {
            this.templateId = fetchProperty("PRIMARY_PARSER_TEMPLATE_ID");
        } else {
            this.templateId = fetchProperty("SECONDARY_PARSER_TEMPLATE_ID");
        }
    }

    public void saveResponse(byte[] bytes, String fileName) {
        java.io.File file;
        if (this.parserType == ParserType.SECONDARY) {
            file = new java.io.File(fetchProperty("SECONDARY_DIR") + fileName.split("\\.")[0] + ".json");
        } else {
            file = new File(fetchProperty("PRIMARY_DIR") + fileName.split("\\.")[0] + ".json");
        }

        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            log.info(String.format("ParsingThread:[%d] fetched %s Parser response for %s.", Thread.currentThread().getId(), this.parserType,
                    fileName));

            os.close();
        } catch (Exception e) {
            log.error(fileName + ".pdf: Exception: " + e);
        }
    }
}
