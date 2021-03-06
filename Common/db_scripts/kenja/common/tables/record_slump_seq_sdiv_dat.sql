-- $Id: caaa7d543f6af9863f1c89531fd20ea243e548b8 $

DROP TABLE RECORD_SLUMP_SEQ_SDIV_DAT

CREATE TABLE RECORD_SLUMP_SEQ_SDIV_DAT( \
    YEAR            VARCHAR(4)    NOT NULL, \
    SEMESTER        VARCHAR(1)    NOT NULL, \
    TESTKINDCD      VARCHAR(2)    NOT NULL, \
    TESTITEMCD      VARCHAR(2)    NOT NULL, \
    SCORE_DIV       VARCHAR(2)    NOT NULL, \
    SLUMP_SEQ       SMALLINT      NOT NULL, \
    CLASSCD         VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD   VARCHAR(2)    NOT NULL, \
    SUBCLASSCD      VARCHAR(6)    NOT NULL, \
    SCHREGNO        VARCHAR(8)    NOT NULL, \
    CHAIRCD         VARCHAR(7), \
    SLUMP           VARCHAR(1), \
    REMARK          VARCHAR(60), \
    SCORE           SMALLINT, \
    MARK            VARCHAR(1), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE RECORD_SLUMP_SEQ_SDIV_DAT ADD CONSTRAINT PK_REC_SL_SEQ_SD PRIMARY KEY (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,SLUMP_SEQ,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)