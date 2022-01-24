-- kanji=漢字
-- $Id: f02e6826058d653247ab28eb4a415c46773e3d1c $

DROP   TABLE RECORD_AVERAGE_CLASS_SDIV_DAT
CREATE TABLE RECORD_AVERAGE_CLASS_SDIV_DAT ( \
    YEAR                VARCHAR(4) NOT NULL, \
    SEMESTER            VARCHAR(1) NOT NULL, \
    TESTKINDCD          VARCHAR(2) NOT NULL, \
    TESTITEMCD          VARCHAR(2) NOT NULL, \
    SCORE_DIV           VARCHAR(2) NOT NULL, \
    CLASS_DIV           VARCHAR(1) NOT NULL, \
    CLASSCD             VARCHAR(2) NOT NULL, \
    SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    AVG_DIV             VARCHAR(1) NOT NULL, \
    GRADE               VARCHAR(2) NOT NULL, \
    HR_CLASS            VARCHAR(3) NOT NULL, \
    COURSECD            VARCHAR(1) NOT NULL, \
    MAJORCD             VARCHAR(3) NOT NULL, \
    COURSECODE          VARCHAR(4) NOT NULL, \
    SCORE               DECIMAL(12,5), \
    AVG                 DECIMAL(9,5), \
    COUNT               SMALLINT, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE RECORD_AVERAGE_CLASS_SDIV_DAT ADD CONSTRAINT PK_REC_AVG_CLS_SD \
      PRIMARY KEY (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASS_DIV, CLASSCD, SCHOOL_KIND, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
