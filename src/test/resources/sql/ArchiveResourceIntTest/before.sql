INSERT INTO audit
    VALUES (
      '1',
      'some uuid',
      '2018-09-01 00:00:00.000', -- Start of covered range - should be returned.
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
        '2018-09-30 23:59:59.999', -- End of covered range - should be returned.
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

INSERT INTO audit
    VALUES (
      '3',
      'another uuid',
      '2018-08-31 23:59:59.999', -- Just before start of covered range - should not be returned.
      '',
      '3a22c723-ea0f-4962-b97b-f35dce3284b2',
      'Audit Service',
      '',
      '',
      'ARCHIVED_RESULTS',
       '{
            "results": {
                "PASS": 999,
                "FAIL": 123
            }
       }'
    );

INSERT INTO audit
VALUES (
        '4',
        'yet some other uuid',
        '2018-10-01 00:00:00.00', -- Just after end of covered range - should not be returned.
        '',
        '3a22c723-ea0f-4962-b97b-f35dce3284b2',
        'Audit Service',
        '',
        '',
        'ARCHIVED_RESULTS',
        '{
            "results": {
                "ERROR": 112,
                "NOTFOUND": 23
                "PASS": 6
            }
       }'

);