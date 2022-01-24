-- $Id: 83df974678050f590f0d06c3f55cde6346ad196d $

DROP TABLE MOCK2_DAT_OLD
RENAME TABLE MOCK2_DAT TO MOCK2_DAT_OLD
CREATE TABLE MOCK2_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    MOCKCD           VARCHAR(9)    NOT NULL, \
    SCHREGNO         VARCHAR(8)    NOT NULL, \
    MOCK_SUBCLASS_CD VARCHAR(6)    NOT NULL, \
    MOCKDIV          VARCHAR(1)    NOT NULL, \
    SCORE            SMALLINT, \
    AVG              DECIMAL(9,5), \
    DEVIATION        DECIMAL(4,1), \
    RANK             SMALLINT, \
    COUNT            SMALLINT, \
    REGISTERCD       VARCHAR(8), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO MOCK2_DAT \
    SELECT \
        YEAR, \
        MOCKCD, \
        SCHREGNO, \
        MOCK_SUBCLASS_CD, \
        MOCKDIV, \
        SCORE, \
        AVG, \
        DEVIATION, \
        RANK, \
        COUNT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        MOCK2_DAT_OLD

ALTER TABLE MOCK2_DAT ADD CONSTRAINT PK_MOCK2_DAT PRIMARY KEY (YEAR,MOCKCD,SCHREGNO,MOCK_SUBCLASS_CD,MOCKDIV)