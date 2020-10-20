package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.isValidParserSelection;
import static TEST_HARNESS.Util.setComparisonParameter;
import java.util.Scanner;

public class ParsePdf {
    public ParsePdf() {
        setParsingParameters();
    }

    public static void setParsingParameters() {
        setComparisonParameter();
        if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("PANDORASTREET") || fetchProperty("PRIMARY_PARSER_NAME")
                .equalsIgnoreCase("PANDORA")) {
            Application.setPrimaryParserName(ParserName.PANDORASTREET);
        } else if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
            Application.setPrimaryParserName(ParserName.OPTIMUS);
        } else if (fetchProperty("PRIMARY_PARSER_NAME").equalsIgnoreCase("BUMBLEBEE")) {
            Application.setPrimaryParserName(ParserName.BUMBLEBEE);
        } else {
            Application.setPrimaryParserName(ParserName.NIL);
        }
        if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("PANDORASTREET")) {
            Application.setSecondaryParserName(ParserName.PANDORASTREET);
        } else if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("OPTIMUS")) {
            Application.setSecondaryParserName(ParserName.OPTIMUS);
        } else if (fetchProperty("SECONDARY_PARSER_NAME").equalsIgnoreCase("BUMBLEBEE")) {
            Application.setSecondaryParserName(ParserName.BUMBLEBEE);
        } else {
            Application.setSecondaryParserName(ParserName.NIL);
            Application.setCompareAgainst(CompareAgainst.STANDALONE);
        }
        if (fetchProperty("PRIMARY_PARSER_ENV").equalsIgnoreCase("STAGE")) {
            Application.setPrimaryParserEnv(Environment.STAGE);
        } else {
            Application.setPrimaryParserEnv(Environment.PROD);
        }
        if (fetchProperty("SECONDARY_PARSER_ENV").equalsIgnoreCase("STAGE")) {
            Application.setSecondaryParserEnv(Environment.STAGE);
        } else {
            Application.setSecondaryParserEnv(Environment.PROD);
        }

    }

    public void fetchParserResponses() {
        if (Application.getCompareAgainst() == CompareAgainst.SECONDARY && isValidParserSelection() == false) {
            System.exit(0);
        }
        ParserResponses bumblebee = new Bumblebee(50.0);
        ParserResponses pandorastreet = new Pandorastreet(50.0);
        ParserResponses optimus = new Optimus(50.0);
        if (Application.getPrimaryParserName() == ParserName.BUMBLEBEE) {
            bumblebee.setParserType(ParserType.PRIMARY);
            bumblebee.fetchAllResponses(Application.getPrimaryParserEnv());

        } else if (Application.getPrimaryParserName() == ParserName.OPTIMUS) {
            optimus.setParserType(ParserType.PRIMARY);
            optimus.fetchAllResponses(Application.getPrimaryParserEnv());

        } else if (Application.getPrimaryParserName() == ParserName.PANDORASTREET) {
            pandorastreet.setParserType(ParserType.PRIMARY);
            pandorastreet.fetchAllResponses(Application.getPrimaryParserEnv());

        }

        if (Application.getCompareAgainst() == CompareAgainst.SECONDARY) {

            if (Application.getSecondaryParserName() == ParserName.BUMBLEBEE) {
                bumblebee.setParserType(ParserType.SECONDARY);
                bumblebee.fetchAllResponses(Application.getSecondaryParserEnv());

            } else if (Application.getSecondaryParserName() == ParserName.PANDORASTREET) {
                pandorastreet.setParserType(ParserType.SECONDARY);
                pandorastreet.fetchAllResponses(Application.getSecondaryParserEnv());

            } else if (Application.getSecondaryParserName() == ParserName.OPTIMUS) {
                optimus.setParserType(ParserType.SECONDARY);
                optimus.fetchAllResponses(Application.getSecondaryParserEnv());

            }
        } else if (Application.getCompareAgainst() == CompareAgainst.MANUAL) {
            CreateSkeletalJsons createSkeletalJsons = new CreateSkeletalJsons();
            createSkeletalJsons.createJsonFiles();
            Scanner sc = new Scanner(System.in);
            int userInput = 0;
            while (userInput != 1) {
                System.out.println(
                        "Make manual changes to skeletal json files in 'ExpectedResponses' directory.To proceed to perform comparison press 1. \n");
                userInput = sc.nextInt();
            }
        }
    }
}
