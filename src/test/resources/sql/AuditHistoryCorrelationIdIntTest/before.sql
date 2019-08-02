-- noinspection SqlNoDataSourceInspectionForFile

INSERT INTO audit
    VALUES (
      '1',
      'some uuid',
      '2017-09-11 14:45:48.094',
      'some session id',
      'correlationID1',
      'some email',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
      'some json'
    );

INSERT INTO audit
VALUES (
        '2',
        'some other uuid',
        '2017-09-11 14:45:55.033',
        'some session id',
        'correlationID1',
        'some email',
        'some deployment',
        'some namespace',
        'INCOME_PROVING_FINANCIAL_STATUS_RESPONSE',
        'some json'
);

INSERT INTO audit
    VALUES (
      '3',
      'yet some other uuid',
      '2017-09-12 14:45:48.014',
      'some session id',
      'correlationID3',
      'some email',
      'some deployment',
      'some namespace',
      'INCOME_PROVING_FINANCIAL_STATUS_REQUEST',
      'some json'
    );

INSERT INTO audit
    VALUES (
      '4',
      'and yet some other uuid',
      '2017-09-11 14:45:48.014',
      'some session id',
      'correlationID2',
      'some email',
      'some deployment',
      'some namespace',
      'ARCHIVED_RESULTS',
      'some json'
    );