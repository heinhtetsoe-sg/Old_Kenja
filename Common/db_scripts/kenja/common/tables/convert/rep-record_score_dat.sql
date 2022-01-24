-- $Id: 3a30aecfb6cc40a4fc431d4ffbd9340679ae413e $

drop table RECORD_SCORE_DAT_OLD
create table RECORD_SCORE_DAT_OLD like RECORD_SCORE_DAT
insert into  RECORD_SCORE_DAT_OLD select * from RECORD_SCORE_DAT

drop   table RECORD_SCORE_DAT
create table RECORD_SCORE_DAT ( \
       YEAR           VARCHAR(4) NOT NULL, \
       SEMESTER       VARCHAR(1) NOT NULL, \
       TESTKINDCD     VARCHAR(2) NOT NULL, \
       TESTITEMCD     VARCHAR(2) NOT NULL, \
       SCORE_DIV      VARCHAR(2) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       CHAIRCD        VARCHAR(7), \
       SCORE          SMALLINT, \
       VALUE          SMALLINT, \
       VALUE_DI       VARCHAR(2), \
       GET_CREDIT     SMALLINT, \
       ADD_CREDIT     SMALLINT, \
       COMP_TAKESEMES VARCHAR(1), \
       COMP_CREDIT    SMALLINT, \
       REGISTERCD     VARCHAR(8), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table RECORD_SCORE_DAT add constraint pk_record_score_dt \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)

insert into RECORD_SCORE_DAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        SCORE_DIV, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        SCHREGNO, \
        CHAIRCD, \
        SCORE, \
        VALUE, \
        VALUE_DI, \
        GET_CREDIT, \
        ADD_CREDIT, \
        COMP_TAKESEMES, \
        COMP_CREDIT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECORD_SCORE_DAT_OLD
