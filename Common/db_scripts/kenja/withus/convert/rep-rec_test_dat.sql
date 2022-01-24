-- $Id: rep-rec_test_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table REC_TEST_DAT_OLD
create table REC_TEST_DAT_OLD like REC_TEST_DAT
insert into  REC_TEST_DAT_OLD select * from REC_TEST_DAT

drop   table REC_TEST_DAT
create table REC_TEST_DAT ( \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    MONTH           varchar(2) not null, \
    SCORE           smallint, \
    CREATOR         varchar(8), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_TEST_DAT add constraint PK_REC_TEST_DAT primary key (YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, MONTH)

insert into REC_TEST_DAT \
  select \
        YEAR, \
        CLASSCD, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        SCHREGNO, \
        MONTH, \
        SCORE, \
        cast(null as varchar(8)), \
        REGISTERCD, \
        UPDATED \
  from REC_TEST_DAT_OLD
