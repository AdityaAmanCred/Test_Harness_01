# How to use this?:

1)Update the **application.properties** file to set the following variables:

## Parser Details:  
PRIMARY_PARSER_NAME: 'BUMBLEBEE' / 'PANDORASTREET' / 'OPTIMUS'  
SECONDARY_PARSER_NAME: 'BUMBLEBEE' / 'PANDORASTREET' / 'OPTIMUS'  
ISSUER: ['Citi Bank','HDFC Bank','SBI,ICICI Bank','Kotak Mahindra Bank','YES Bank','IndusInd Bank','Standard Chartered Bank','Axis Bank','RBL Bank','HSBC Bank','AMEX']    
PRIMARY_PARSER_TEMPLATE_ID: Template id for the primary parser    
SECONDARY_PARSER_TEMPLATE_ID : Template id for the secondary parser  
PRIMARY_PARSER_ENV= 'STAGE' / 'PROD'  
SECONDARY_PARSER_ENV='STAGE' / 'PROD'  
COMPARISON_MODE: 'SECONDARY'/'STANDALONE'(Pick Secondary if you wish to compare a pair{parser,template_id} against another)   
SECONDARY_MODE_STANDALONE_ANALYSIS_DISABLED=false/true  
PDF_DOWNLOAD_LOC: Absolute location on your local system where PDFs will be downloaded to.  
PRIMARY_DIR: Absolute location where Primary parser transformed responses for these PDFs are saved to.  
SECONDARY_DIR: Absolute location where Secondary parser transformed responses for these PDFs are saved to.  
RESULT_DIR: Absolute location where Test Harness results are saved to.  
FILE_IDS_CSV: Absolute location of the csv file containing object_ids, user_ids for PDFs(Downloaded from Metabase, Check test_harness channel for the query).    
KEY_FILTER= (This string determines what set of fields(keys) will be taken into consideration by comparator.Refer 'GUIDELINES FOR KEY_FILTER:' below for more.)  

##  GUIDELINES FOR KEY_FILTER:

For the filtering examples, let's use an the example with the JSON below. 
```json
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
```

1) **Select all**(All the 'keys' present in JSON): '**'  

2) **Select a single field:**  'assignee.firstName'  

3) **Select set of fields:** 'id,actions,properties.email' (Note use of a dot as delimiter to convey nested json structure)  

5) **exclude a field or set of fields:**   
   '-assignee.lastName' (Note use of '-' sign at the beginning)  
   '-assignee.lastName,-actions.0.user.firstName' (Not use of number 0 to indicate index   of that particular json in parent JSONARRAY)  


## CONCURRENCY_AND_RATE_LIMIT_Details  
NUM_DOWNLOAD_THREADS=n(Where n is an integer that represents the fixed number of download threads, you want to spawn.)
DOWNLOAD_RATELIMIT=r(Where r is a decimal that represents download rate limit)  
NUM_PRIMARYPARSER_THREADS=n(Where n is an integer that represents the fixed number of threads, you want to spawn for your primary parser)  
PRIMARY_PARSER_RATELIMIT=r(Where r is a decimal that represents parsing rate-limit for primary parser)    
NUM_SECONDARYPARSER_THREADS=n(Where n is an integer that represents fixed number of threads, you want to spawn for your secondary parser)  
SECONDARY_PARSER_RATELIMIT=r(Where r is a decimal that represents parsing rate-limit for secondary parser) 

## Transaction Inspection

The class **TransactionsInspector.java**'s method 'inspectTransactions\(\)' does the following:

* Compares the domestic-transactions captured by the two parsers and collates the information file-wise in TransactionsInspectionResults.json as under:

1. Primary\_Parser\_Transaction\_Capture\_Failures : Cases wherein transactions that were captured by Secondary parser but not captured by Primary parser.
2. Secondary\_Parser\_Transaction\_Capture\_Failures : Cases wherein transactions that were captured by Primary but not captured by Secondary parser.
3. Other\_Transaction\_Differences: Cases that do not fall in either of above two categories.

* Captures those transactions whose transaction-amount has been captured as a negative number by the Primary parser. 

**NOTE**: The first part is implemented using hashing. This solves for issues arising out of index based comparison where in if one parsers fails to capture one of more transactions and the other parser did, the whole comparison would be rendered futile.  



## Note:
* Name the directories on your local identical to the directory names mentioned under 'Parser Details' section.  
['PDF_DOWNLOAD_LOC,'PRIMARY_DIR','SECONDARY_DIR','RESULT_DIR']  

