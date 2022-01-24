-- $Id: rep-rec_report_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table REC_REPORT_DAT_OLD
create table REC_REPORT_DAT_OLD like REC_REPORT_DAT
insert into  REC_REPORT_DAT_OLD select * from REC_REPORT_DAT

drop   table REC_REPORT_DAT
create table REC_REPORT_DAT ( \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    REPORT_SEQ      smallint not null, \
    COMMITED_DATE1  date, \
    COMMITED_DATE2  date, \
    COMMITED_SCORE1 smallint, \
    COMMITED_SCORE2 smallint, \
    CREATOR         varchar(8), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_REPORT_DAT add constraint PK_REC_REPORT_DAT primary key (YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, REPORT_SEQ)

insert into REC_REPORT_DAT \
  select \
        YEAR, \
        CLASSCD, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        SCHREGNO, \
        REPORT_SEQ, \
        COMMITED_DATE1, \
        COMMITED_DATE2, \
        COMMITED_SCORE1, \
        COMMITED_SCORE2, \
        cast(null as varchar(8)), \
        REGISTERCD, \
        UPDATED \
  from REC_REPORT_DAT_OLD
