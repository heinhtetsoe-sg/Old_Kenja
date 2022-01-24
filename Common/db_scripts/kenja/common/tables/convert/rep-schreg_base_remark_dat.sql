-- $Id: 76bdf6bb9877433c1cbf5db57cb5e66ffb5bfaf2 $

DROP TABLE SCHREG_BASE_REMARK_DAT_OLD
RENAME TABLE SCHREG_BASE_REMARK_DAT TO SCHREG_BASE_REMARK_DAT_OLD
CREATE TABLE SCHREG_BASE_REMARK_DAT( \
        YEAR                VARCHAR(4)    NOT NULL, \
        SCHREGNO            VARCHAR(8)    NOT NULL, \
        CODE                VARCHAR(2)    NOT NULL, \
        SEQ                 VARCHAR(2)    NOT NULL, \
        REMARK              VARCHAR(410)  , \
        ANSWER_PATTERN      VARCHAR(1)    , \
        REGISTERCD  VARCHAR(10), \
        UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_BASE_REMARK_DAT ADD CONSTRAINT PK_SCH_BASE_REMARK PRIMARY KEY (YEAR, SCHREGNO, CODE, SEQ)

INSERT INTO SCHREG_BASE_REMARK_DAT \
    SELECT \
        YEAR,           \
        SCHREGNO,       \
        CODE,           \
        SEQ,            \
        REMARK,         \
        CAST(NULL AS VARCHAR(1)) AS ANSWER_PATTERN,  \
        REGISTERCD,     \
        UPDATED         \
    FROM \
        SCHREG_BASE_REMARK_DAT_OLD
