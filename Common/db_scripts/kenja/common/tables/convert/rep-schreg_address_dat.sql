-- $Id: b419cc0dd7b3dadbf8ead20aa1f1151750d048a6 $

DROP TABLE SCHREG_ADDRESS_DAT_OLD
RENAME TABLE SCHREG_ADDRESS_DAT TO SCHREG_ADDRESS_DAT_OLD
CREATE TABLE SCHREG_ADDRESS_DAT( \
    SCHREGNO    VARCHAR(8)    NOT NULL, \
    ISSUEDATE   DATE          NOT NULL, \
    EXPIREDATE  DATE, \
    ZIPCD       VARCHAR(8), \
    AREACD      VARCHAR(2), \
    ADDR1       VARCHAR(150), \
    ADDR2       VARCHAR(150), \
    ADDR_FLG    VARCHAR(1), \
    ADDR1_ENG   VARCHAR(150), \
    ADDR2_ENG   VARCHAR(150), \
    TELNO       VARCHAR(14), \
    TELNO_MEMO  VARCHAR(24), \
    TELNO2      VARCHAR(14), \
    TELNO2_MEMO VARCHAR(24), \
    FAXNO       VARCHAR(14), \
    EMAIL       VARCHAR(50), \
    REGISTERCD  VARCHAR(10), \
    UPDATED     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO SCHREG_ADDRESS_DAT \
    SELECT \
        SCHREGNO, \
        ISSUEDATE, \
        EXPIREDATE, \
        ZIPCD, \
        AREACD, \
        ADDR1, \
        ADDR2, \
        ADDR_FLG, \
        ADDR1_ENG, \
        ADDR2_ENG, \
        TELNO, \
        CAST(NULL AS VARCHAR(24)) AS TELNO_MEMO, \
        TELNO2, \
        CAST(NULL AS VARCHAR(24)) AS TELNO2_MEMO, \
        FAXNO, \
        EMAIL, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SCHREG_ADDRESS_DAT_OLD

ALTER TABLE SCHREG_ADDRESS_DAT ADD CONSTRAINT PK_SCHREG_ADDRESS PRIMARY KEY (SCHREGNO,ISSUEDATE)