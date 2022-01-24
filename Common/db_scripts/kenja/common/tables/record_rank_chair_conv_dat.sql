-- $Id: 9283cf2a9d19fe324b4da5c5c98f6cf8fa274e3d $

DROP TABLE RECORD_RANK_CHAIR_CONV_DAT
CREATE TABLE RECORD_RANK_CHAIR_CONV_DAT( \
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

ALTER TABLE RECORD_RANK_CHAIR_CONV_DAT ADD CONSTRAINT PK_REC_RANK_CHR_CV PRIMARY KEY (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CHAIRCD,SCHREGNO)