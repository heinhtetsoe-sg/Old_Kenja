-- $Id: 870d44093abb9e558b2181be5e3bdbc3221ff502 $

drop   table SUPP_EXA_DAT_OLD
rename table SUPP_EXA_DAT TO SUPP_EXA_DAT_OLD

create table SUPP_EXA_DAT( \
     YEAR           VARCHAR(4) NOT NULL, \
     SEMESTER       VARCHAR(1) NOT NULL, \
     TESTKINDCD     VARCHAR(2) NOT NULL, \
     TESTITEMCD     VARCHAR(2) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     CURRICULUM_CD  VARCHAR(2) NOT NULL, \
     SUBCLASSCD     VARCHAR(6) NOT NULL, \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     SCORE          SMALLINT, \
     SCORE_PASS     SMALLINT, \
     SCORE_FLG      VARCHAR(1), \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

insert into SUPP_EXA_DAT \
    select \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        LEFT(SUBCLASSCD, 2) AS COMBINED_CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        SCHREGNO, \
        SCORE, \
        SCORE_PASS, \
        SCORE_FLG, \
        REGISTERCD, \
        UPDATED \
    from SUPP_EXA_DAT_OLD

alter table SUPP_EXA_DAT add constraint pk_supp_exa_dt primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)

