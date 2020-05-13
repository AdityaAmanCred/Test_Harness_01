How to use this? :

1)Edit the application.properties file to set the following variables:

STAGE_PARSER_NAME: BUMBLEBEE / PROD /OPTIMUS
PROD_PARSER_NAME: BUMBLEBEE / PROD /OPTIMUS
STAGE_ID: Template id on stage
PROD_ID : Template id on prod
PDF_DOWNLOAD_LOC: Absolute location where PDFs will first be downloaeded to and later fetched from.
EXPECTED_DIR: Absolute location where either prod responses or skeletal jsons(Json response with all null fields) will be saved to. Later these need to be manually annotated.
STAGE_DIR: Absolute location where stage responses are saved to.
RESULT_DIR: Absolute location where results are saved to.
COMPARISON_MODE: PROD/MANUAL/STANDALONE
NULLCHECK_FIELDS_CSV: Absolute location of CSV file that contains data-fields that you assert must exist for each file/isin etc.(optional)
FILE_IDS_CSV: Absolute location of csv file containing ids(pdf ids). Make sure the ids are sans quotes.(Used only for Parser testing/validation)
ISIN_CSV: Absolute location of csv file containing ISINs.(For track related testing)
KEY_FILTER= This string determines what set of fields will be taken into consideration by comparator.REFER GUIDELINES BELOW FOR MORE

2)Run the main.


GUIDELINES FOR KEY_FILTER:

For the filtering examples, let's use an the example object of type Issue

{
  "id": "ISSUE-1",
  "issueSummary": "Dragons Need Fed",
  "issueDetails": "I need my dragons fed pronto.",
  "reporter": {
    "firstName": "Daenerys",
    "lastName": "Targaryen"
  },
  "assignee": {
    "firstName": "Jorah",
    "lastName": "Mormont"
  },
  "actions": [
    {
      "id": null,
      "type": "COMMENT",
      "text": "I'm going to let Daario get this one.",
      "user": {
        "firstName": "Jorah",
        "lastName": "Mormont"
      }
    },
    {
      "id": null,
      "type": "CLOSE",
      "text": "All set.",
      "user": {
        "firstName": "Daario",
        "lastName": "Naharis"
      }
    }
  ],
  "properties": {
    "priority": "1",
    "email": "motherofdragons@got.com"
  }
}

1) Select all(ENTIRE JSON As IS):
   **

2) Select a single field:
   assignee.firstName

3) Select set of fields:
   id,actions,properties.email

5) exclude a field:
   -assignee.lastName

