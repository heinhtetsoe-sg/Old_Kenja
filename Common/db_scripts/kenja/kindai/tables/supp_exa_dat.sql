--追試験データ
--2004-11-09 新規作成

DROP TABLE SUPP_EXA_DAT

CREATE TABLE SUPP_EXA_DAT  \
(  \
        YEAR                  VARCHAR(4) NOT NULL, \
        CLASSCD               VARCHAR(2) NOT NULL, \
        SCHOOL_KIND           VARCHAR(2) NOT NULL, \
        CURRICULUM_CD         VARCHAR(2) NOT NULL, \
        SUBCLASSCD            VARCHAR(6) NOT NULL, \
        SCHREGNO              VARCHAR(8) NOT NULL, \
        SCORE                 SMALLINT, \
        DI_MARK               VARCHAR(2), \
        GRADE_RECORD          SMALLINT, \
        A_PATTERN_ASSESS      VARCHAR(1), \
        B_PATTERN_ASSESS      VARCHAR(1), \
        C_PATTERN_ASSESS      VARCHAR(1), \
        JUDGE_PATTERN         VARCHAR(1), \
        REGISTERCD            VARCHAR(8), \
        UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUPP_EXA_DAT  \
ADD CONSTRAINT PK_SUPP_EXA_DAT  \
PRIMARY KEY  \
(YEAR,SUBCLASSCD,SCHREGNO)
