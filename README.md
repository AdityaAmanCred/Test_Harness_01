How to use this?:

1)Update the application.properties file to set the following variables:

PRIMARY_PARSER_NAME: 'BUMBLEBEE' / 'PANDORASTREET' / 'OPTIMUS'
SECONDARY_PARSER_NAME: 'BUMBLEBEE' / 'PANDORASTREET' / 'OPTIMUS'
PRIMARY_PARSER_TEMPLATE_ID: Template id for primary parser
SECONDARY_PARSER_TEMPLATE_ID : Template id on secondary parser
PRIMARY_PARSER_ENV= 'STAGE' / 'PROD'
SECONDARY_PARSER_ENV='STAGE' / 'PROD'
COMPARISON_MODE: 'SECONDARY'/'STANDALONE'(The former is the one almost always used ;ie when you are comparing PRIMARY responses to SECONDARY responses)
PDF_DOWNLOAD_LOC: Absolute location on your local system where PDFs will first be downloaeded to.
PRIMARY_DIR: Absolute location where Primary parser transformed responses for these PDFs are saved to.
SECONDARY_DIR: Absolute location where Secondary parser transformed responses for these PDFs are saved to.
RESULT_DIR: Absolute location where Test Harness results are saved to.
FILE_IDS_CSV: Absolute location of csv file containing ids(pdf ids).
KEY_FILTER= This string determines what set of fields(keys) will be taken into consideration by comparator.Refer 'GUIDELINES FOR KEY_FILTER:' below for more.
//Morning Star
ISIN_CSV: Absolute location of csv file containing ISINs.(For track related testing)

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

1) Select all(All the 'keys' present in JSON):
   **

2) Select a single field:
   example:
   assignee.firstName

3) Select set of fields:
   example:
   id,actions,properties.email (Note use of a dot as delimiter to convey nested json structure)

5) exclude a field or set of fields:
   -assignee.lastName(Note use of '-' sign at the beginning)
   -assignee.lastName,-actions.0.user.firstName(Not use of number 0 to indicate index of that particular json in parent JSONARRAY)