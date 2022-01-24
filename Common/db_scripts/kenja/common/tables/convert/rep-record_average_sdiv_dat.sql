-- $Id: 9d363103b4ce59afd71682a9a6ab91c584d28973 $

DROP TABLE RECORD_AVERAGE_SDIV_DAT_OLD
RENAME TABLE RECORD_AVERAGE_SDIV_DAT TO RECORD_AVERAGE_SDIV_DAT_OLD
CREATE TABLE RECORD_AVERAGE_SDIV_DAT( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    TESTKINDCD      VARCHAR(2) NOT NULL, \
    TESTITEMCD      VARCHAR(2) NOT NULL, \
    SCORE_DIV       VARCHAR(2) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    AVG_DIV         VARCHAR(1) NOT NULL, \
    GRADE           VARCHAR(2) NOT NULL, \
    HR_CLASS        VARCHAR(3) NOT NULL, \
    COURSECD        VARCHAR(1) NOT NULL, \
    MAJORCD         VARCHAR(3) NOT NULL, \
    COURSECODE      VARCHAR(4) NOT NULL, \
    SCORE           INTEGER, \
    HIGHSCORE       INTEGER, \
    LOWSCORE        INTEGER, \
    COUNT           SMALLINT, \
    AVG             DECIMAL (9,5), \
    STDDEV          DECIMAL (5,1), \
    HIGHSCORE_AVG   DECIMAL (9,5), \
    LOWSCORE_AVG    DECIMAL (9,5), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO RECORD_AVERAGE_SDIV_DAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        SCORE_DIV, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        AVG_DIV, \
        GRADE, \
        HR_CLASS, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        SCORE, \
        HIGHSCORE, \
        LOWSCORE, \
        COUNT, \
        AVG, \
        STDDEV, \
        CAST(NULL AS DECIMAL(9,5)) AS HIGHSCORE_AVG, \
        CAST(NULL AS DECIMAL(9,5)) AS LOWSCORE_AVG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECORD_AVERAGE_SDIV_DAT_OLD

ALTER TABLE RECORD_AVERAGE_SDIV_DAT ADD CONSTRAINT PK_REC_AVG_SD_DAT \
      PRIMARY KEY (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)