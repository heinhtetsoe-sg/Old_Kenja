-- kanji=´Á»ú
-- $Id: c9d70cf5b403369279ec1c5e00ee5f17a372d4a5 $
-- ²ÊÌÜ¹çÊ»À®ÀÓ¼«Æ°·×»»ÍúÎò¥Ç¡¼¥¿

drop table COMB_GCALC_EXEC_TESTITEM_DAT

create table COMB_GCALC_EXEC_TESTITEM_DAT(  \
    CALC_DATE                   DATE NOT NULL, \
    CALC_TIME                   TIME NOT NULL, \
    COMBINED_CLASSCD            VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    GVAL_CALC                   VARCHAR(1), \
    YEAR                        VARCHAR(4), \
    SEMESTER                    VARCHAR(1), \
    TESTKINDCD                  VARCHAR(2), \
    TESTITEMCD                  VARCHAR(2), \
    SCORE_DIV                   VARCHAR(2), \
    REGISTERCD                  VARCHAR(10), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table COMB_GCALC_EXEC_TESTITEM_DAT add constraint PK_COM_GC_EXE_T_D \
primary key (CALC_DATE, CALC_TIME, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD)
