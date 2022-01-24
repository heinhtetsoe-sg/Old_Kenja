-- $Id: 2b8da44ae47ae4c1d49ab4de6089b726518f9c30 $

DROP TABLE RECORD_RANK_CHAIR_V_DAT_OLD
RENAME TABLE RECORD_RANK_CHAIR_V_DAT TO RECORD_RANK_CHAIR_V_DAT_OLD
CREATE TABLE RECORD_RANK_CHAIR_V_DAT( \
    YEAR                  VARCHAR(4)    NOT NULL, \
    SEMESTER              VARCHAR(1)    NOT NULL, \
    TESTKINDCD            VARCHAR(2)    NOT NULL, \
    TESTITEMCD            VARCHAR(2)    NOT NULL, \
    CLASSCD               VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND           VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD         VARCHAR(2)    NOT NULL, \
    SUBCLASSCD            VARCHAR(6)    NOT NULL, \
    CHAIRCD               VARCHAR(7)    NOT NULL, \
    SCHREGNO              VARCHAR(8)    NOT NULL, \
    SCORE                 SMALLINT, \
    AVG                   DECIMAL(8,5), \
    GRADE_RANK            SMALLINT, \
    GRADE_AVG_RANK        SMALLINT, \
    GRADE_DEVIATION       DECIMAL(4,1), \
    GRADE_DEVIATION_RANK  SMALLINT, \
    CLASS_RANK            SMALLINT, \
    CLASS_AVG_RANK        SMALLINT, \
    CLASS_DEVIATION       DECIMAL(4,1), \
    CLASS_DEVIATION_RANK  SMALLINT, \
    COURSE_RANK           SMALLINT, \
    COURSE_AVG_RANK       SMALLINT, \
    COURSE_DEVIATION      DECIMAL(4,1), \
    COURSE_DEVIATION_RANK SMALLINT, \
    MAJOR_RANK            SMALLINT, \
    MAJOR_AVG_RANK        SMALLINT, \
    MAJOR_DEVIATION       DECIMAL(4,1), \
    MAJOR_DEVIATION_RANK  SMALLINT, \
    CHAIRDATE             DATE, \
    REGISTERCD            VARCHAR(8), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO RECORD_RANK_CHAIR_V_DAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        CHAIRCD, \
        SCHREGNO, \
        SCORE, \
        AVG, \
        GRADE_RANK, \
        GRADE_AVG_RANK, \
        GRADE_DEVIATION, \
        GRADE_DEVIATION_RANK, \
        CLASS_RANK, \
        CLASS_AVG_RANK, \
        CLASS_DEVIATION, \
        CLASS_DEVIATION_RANK, \
        COURSE_RANK, \
        COURSE_AVG_RANK, \
        COURSE_DEVIATION, \
        COURSE_DEVIATION_RANK, \
        MAJOR_RANK, \
        MAJOR_AVG_RANK, \
        MAJOR_DEVIATION, \
        MAJOR_DEVIATION_RANK, \
        CHAIRDATE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECORD_RANK_CHAIR_V_DAT_OLD

ALTER TABLE RECORD_RANK_CHAIR_V_DAT ADD CONSTRAINT PK_REC_R_CHR_V_DT PRIMARY KEY (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CHAIRCD,SCHREGNO)