INSERT INTO audit
    VALUES (
      '1',
      'some uuid',
      '2017-09-11 14:45:48.094',
      'some session id',
      'corr id 1',
      'bobby.bag@digital.homeoffice.gov.uk',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
       '{
        "forename": "Antonio",
        "method": "get-financial-status",
        "dependants": 0,
        "surname": "Gramsci",
        "applicationRaisedDate": "2017-06-01",
        "dateOfBirth": "1891-01-22",
        "nino": "NE112233C"
        }'
    );

INSERT INTO audit
VALUES (
        '2',
        'some uuid 2',
        '2017-09-11 14:45:55.033',
        'some session id',
        'corr id 2',
        'bobby.bag@digital.homeoffice.gov.uk',
        'some deployment',
        'some namespace',
        'INCOME_PROVING_FINANCIAL_STATUS_RESPONSE',
        '{
          "method": "get-financial-status",
          "response": {
            "status": {
              "code": "100",
              "message": "OK"
            },
            "individual": {
              "title": "",
              "forename": "Antonio",
              "surname": "Gramsci",
              "nino": "NE112233C"
            },
            "categoryCheck": {
              "category": "A",
              "passed": false,
              "applicationRaisedDate": "2017-06-01",
              "assessmentStartDate": "2016-12-01",
              "failureReason": "MONTHLY_VALUE_BELOW_THRESHOLD",
              "threshold": 1550,
              "employers": [
                "MCDONALDS"
              ]
            }
          }
        }'
);INSERT INTO audit
    VALUES (
      '3',
      'some uuid 3',
      '2017-09-12 14:45:48.094',
      'some session id',
      'corr id 3',
      'bobby.bag@digital.homeoffice.gov.uk',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
       '{
        "forename": "Antonio",
        "method": "get-financial-status",
        "dependants": 0,
        "surname": "Gramsci",
        "applicationRaisedDate": "2017-06-01",
        "dateOfBirth": "1891-01-22",
        "nino": "NE112233C"
        }'
    );

INSERT INTO audit
VALUES (
        '4',
        'some uuid 4',
        '2017-09-12 14:45:55.033',
        'some session id',
        'corr id 4',
        'bobby.bag@digital.homeoffice.gov.uk',
        'some deployment',
        'some namespace',
        'INCOME_PROVING_FINANCIAL_STATUS_RESPONSE',
        '{
          "method": "get-financial-status",
          "response": {
            "status": {
              "code": "100",
              "message": "OK"
            },
            "individual": {
              "title": "",
              "forename": "Antonio",
              "surname": "Gramsci",
              "nino": "NE112233C"
            },
            "categoryCheck": {
              "category": "A",
              "passed": false,
              "applicationRaisedDate": "2017-06-01",
              "assessmentStartDate": "2016-12-01",
              "failureReason": "MONTHLY_VALUE_BELOW_THRESHOLD",
              "threshold": 1550,
              "employers": [
                "MCDONALDS"
              ]
            }
          }
        }'
);INSERT INTO audit
    VALUES (
      '5',
      'some uuid 5',
      '2017-09-13 14:45:48.094',
      'some session id',
      'corr id 5',
      'bobby.bag@digital.homeoffice.gov.uk',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
       '{
        "forename": "Antonio",
        "method": "get-financial-status",
        "dependants": 0,
        "surname": "Gramsci",
        "applicationRaisedDate": "2017-06-01",
        "dateOfBirth": "1891-01-22",
        "nino": "NE112233C"
        }'
    );

INSERT INTO audit
VALUES (
        '6',
        'some uuid 6',
        '2017-09-13 14:45:55.033',
        'some session id',
        'corr id 6',
        'bobby.bag@digital.homeoffice.gov.uk',
        'some deployment',
        'some namespace',
        'INCOME_PROVING_FINANCIAL_STATUS_RESPONSE',
        '{
          "method": "get-financial-status",
          "response": {
            "status": {
              "code": "100",
              "message": "OK"
            },
            "individual": {
              "title": "",
              "forename": "Antonio",
              "surname": "Gramsci",
              "nino": "NE112233C"
            },
            "categoryCheck": {
              "category": "A",
              "passed": false,
              "applicationRaisedDate": "2017-06-01",
              "assessmentStartDate": "2016-12-01",
              "failureReason": "MONTHLY_VALUE_BELOW_THRESHOLD",
              "threshold": 1550,
              "employers": [
                "MCDONALDS"
              ]
            }
          }
        }'
);

INSERT INTO audit
VALUES (
        '50',
        'some uuid 50',
        '2017-09-02 00:00:00.000',
        '',
        'corr id 50',
        'Audit Service',
        '',
        '',
        'ARCHIVED_RESULTS',
        '{
           "results": {
              "PASS": 1,
              "FAIL": 2
           }
        }'
);


