--ÄÉ»î¸³¥Ç¡¼¥¿
--$Id: rep-supp_exa_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

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

insert into SUPP_EXA_DAT \
    select \
        YEAR, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        SCHREGNO, \
        SCORE, \
        DI_MARK, \
        GRADE_RECORD, \
        A_PATTERN_ASSESS, \
        B_PATTERN_ASSESS, \
        C_PATTERN_ASSESS, \
        JUDGE_PATTERN, \
        UPDATED \
    from SUPP_EXA_DAT_OLD

ALTER TABLE SUPP_EXA_DAT  \
ADD CONSTRAINT PK_SUPP_EXA_DAT  \
PRIMARY KEY  \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)
