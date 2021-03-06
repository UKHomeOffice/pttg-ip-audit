-- noinspection SqlNoDataSourceInspectionForFile

INSERT INTO audit
    VALUES (
      '1',
      'some uuid',
      '2017-09-11 14:45:48.094',
      'some session id',
      'correlation-id-1',
      'some email',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
       '{
        "forename": "some forename",
        "method": "get-financial-status",
        "dependants": 0,
        "surname": "some surname",
        "applicationRaisedDate": "2017-06-01",
        "dateOfBirth": "1891-01-22",
        "nino": "some nino"
        }'
    );

INSERT INTO audit
VALUES (
        '2',
        'another uuid',
        '2017-09-11 14:45:55.033',
        'some session id',
        'correlation-id-1',
        'some email',
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
              "forename": "some forename",
              "surname": "some surname",
              "nino": "some nino"
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
      '3',
      'some other uuid',
      '2017-09-11 14:45:48.094',
      'some session id',
      'correlation-id-2', --Should be excluded from results as it is a different correlationId
      'some email',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
       '{
        "forename": "some forename",
        "method": "get-financial-status",
        "dependants": 0,
        "surname": "some surname",
        "applicationRaisedDate": "2017-06-01",
        "dateOfBirth": "1891-01-22",
        "nino": "some nino"
        }'
    );

INSERT INTO audit
    VALUES (
      '4',
      'yet some other uuid',
      '2017-09-11 14:45:48.094',
      'some session id',
      'correlation-id-1',
      'some email',
      'some deployment',
      'some namespace',
      'DWP_BENEFITS_REQUEST', --Should be excluded from results as Audit Event Type is not one of the types requested
       '{
        "forename": "some forename",
        "method": "get-financial-status",
        "dependants": 0,
        "surname": "some surname",
        "applicationRaisedDate": "2017-06-01",
        "dateOfBirth": "1891-01-22",
        "nino": "some nino"
        }'
    );
