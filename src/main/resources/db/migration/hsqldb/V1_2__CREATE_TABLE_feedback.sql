CREATE TABLE IF NOT EXISTS feedback
(
    id         BIGSERIAL PRIMARY KEY,
    uuid       VARCHAR(255)  NOT NULL,
    timestamp  TIMESTAMP     NOT NULL,
    session_id VARCHAR(255)  NOT NULL,
    deployment VARCHAR(255)  NOT NULL,
    namespace  VARCHAR(255)  NOT NULL,
    user_id    VARCHAR(255)  NOT NULL,
    nino       VARCHAR(255)  NOT NULL,
    detail     VARCHAR(1024) NOT NULL
);
