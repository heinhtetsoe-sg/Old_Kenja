-- kanji=漢字
-- $Id: f1715f2fd15bd1a0b96a1cf7fa98a9e989d9c6f1 $

DROP TABLE RECORD_RANK_CHAIR_SDIV_DAT
CREATE TABLE RECORD_RANK_CHAIR_SDIV_DAT \
(  \
    YEAR                  VARCHAR(4)    NOT NULL, \
    SEMESTER              VARCHAR(1)    NOT NULL, \
    TESTKINDCD            VARCHAR(2)    NOT NULL, \
    TESTITEMCD            VARCHAR(2)    NOT NULL, \
    SCORE_DIV             VARCHAR(2)    NOT NULL, \
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
    COURSE_GROUP_RANK           SMALLINT, \
    COURSE_GROUP_AVG_RANK       SMALLINT, \
    COURSE_GROUP_DEVIATION      DECIMAL(4,1), \
    COURSE_GROUP_DEVIATION_RANK SMALLINT, \
    CHAIR_GROUP_RANK            SMALLINT, \
    CHAIR_GROUP_AVG_RANK        SMALLINT, \
    CHAIR_GROUP_DEVIATION       DECIMAL(4,1), \
    CHAIR_GROUP_DEVIATION_RANK  SMALLINT, \
    CHAIRDATE             DATE, \
    REGISTERCD            VARCHAR(10), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE RECORD_RANK_CHAIR_SDIV_DAT ADD CONSTRAINT PK_REC_R_CHR_SD_DT PRIMARY KEY (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CHAIRCD,SCHREGNO)