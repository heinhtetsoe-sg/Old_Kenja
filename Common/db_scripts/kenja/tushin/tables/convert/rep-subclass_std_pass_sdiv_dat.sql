-- $Id: rep-subclass_std_pass_sdiv_dat.sql 61672 2018-08-07 02:02:45Z yamashiro $

drop table SUBCLASS_STD_PASS_SDIV_DAT_OLD
create table SUBCLASS_STD_PASS_SDIV_DAT_OLD like SUBCLASS_STD_PASS_SDIV_DAT
insert into SUBCLASS_STD_PASS_SDIV_DAT_OLD select * from SUBCLASS_STD_PASS_SDIV_DAT

drop table SUBCLASS_STD_PASS_SDIV_DAT

create table SUBCLASS_STD_PASS_SDIV_DAT( \
    YEAR                varchar(4)  not null, \
    SEMESTER            varchar(1)  not null, \
    TESTKINDCD          varchar(2)  not null, \
    TESTITEMCD          varchar(2)  not null, \
    SCORE_DIV           varchar(2)  not null, \
    CLASSCD             varchar(2)  not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    CURRICULUM_CD       varchar(2)  not null, \
    SUBCLASSCD          varchar(6)  not null, \
    SCHREGNO            varchar(8)  not null, \
    REP_PASS_FLG        varchar(1) , \
    SCHOOLING_PASS_FLG  varchar(1) , \
    RECORD_PASS_FLG     varchar(1) , \
    SEM_PASS_FLG        varchar(1) , \
    GRAD_PASS_FLG       varchar(1) , \
    PRINT_FLG           varchar(1) , \
    REGISTERCD          varchar(8) , \
    UPDATED             timestamp   default current timestamp \
 ) in usr1dms index in idx1dms

alter table SUBCLASS_STD_PASS_SDIV_DAT add constraint PK_SBCLS_S_PSD_DAT primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)

INSERT INTO SUBCLASS_STD_PASS_SDIV_DAT \
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
         SCHREGNO, \
         REP_PASS_FLG, \
         SCHOOLING_PASS_FLG, \
         RECORD_PASS_FLG, \
         SEM_PASS_FLG, \
         GRAD_PASS_FLG, \
         cast(null as varchar(1)) AS PRINT_FLG, \
         REGISTERCD, \
         UPDATED \
     FROM \
         SUBCLASS_STD_PASS_SDIV_DAT_OLD
