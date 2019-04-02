INSERT INTO audit
    VALUES (
      '1',
      'some uuid',
      '2018-09-11 14:45:48.094',
      '',
      '3a22c723-ea0f-4962-b97b-f35dce3284b2',
      'Audit Service',
      '',
      '',
      'ARCHIVED_RESULTS',
       '{
            "results": {
                "PASS": 5,
                "FAIL": 3
            }
       }'
    );

INSERT INTO audit
VALUES (
        '2',
        'some other uuid',
        '2018-09-11 14:45:55.033',
        '',
        '3a22c723-ea0f-4962-b97b-f35dce3284b2',
        'Audit Service',
        '',
        '',
        'ARCHIVED_RESULTS',
        '{
            "results": {
                "ERROR": 2,
                "NOTFOUND": 99
            }
       }'

);