-- $Id: 9ed72ba445e327b8d4df0996109057c2ad5eec85 $

DROP TABLE RECORD_SLUMP_DAT_OLD
RENAME TABLE RECORD_SLUMP_DAT TO RECORD_SLUMP_DAT_OLD
CREATE TABLE RECORD_SLUMP_DAT( \
    YEAR            VARCHAR(4)    NOT NULL, \
    SEMESTER        VARCHAR(1)    NOT NULL, \
    TESTKINDCD      VARCHAR(2)    NOT NULL, \
    TESTITEMCD      VARCHAR(2)    NOT NULL, \
    CLASSCD         VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD   VARCHAR(2)    NOT NULL, \
    SUBCLASSCD      VARCHAR(6)    NOT NULL, \
    SCHREGNO        VARCHAR(8)    NOT NULL, \
    CHAIRCD         VARCHAR(7), \
    SLUMP           VARCHAR(1), \
    REMARK          VARCHAR(60), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO RECORD_SLUMP_DAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        SCHREGNO, \
        CHAIRCD, \
        SLUMP, \
        REMARK, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECORD_SLUMP_DAT_OLD

ALTER TABLE RECORD_SLUMP_DAT ADD CONSTRAINT PK_RECORD_SLUM_DAT PRIMARY KEY (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)