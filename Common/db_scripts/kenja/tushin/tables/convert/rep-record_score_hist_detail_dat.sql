-- kanji=����
-- $Id: rep-record_score_hist_detail_dat.sql 61699 2018-08-08 07:23:27Z yamashiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_SCORE_HIST_DETAIL_DAT_OLD
create table RECORD_SCORE_HIST_DETAIL_DAT_OLD like RECORD_SCORE_HIST_DETAIL_DAT
insert into RECORD_SCORE_HIST_DETAIL_DAT_OLD select * from RECORD_SCORE_HIST_DETAIL_DAT

drop table RECORD_SCORE_HIST_DETAIL_DAT
create table RECORD_SCORE_HIST_DETAIL_DAT \
      (YEAR           varchar(4) not null, \
       SEMESTER       varchar(1) not null, \
       TESTKINDCD     varchar(2) not null, \
       TESTITEMCD     varchar(2) not null, \
       SCORE_DIV      varchar(2) not null, \
       CLASSCD        varchar(2) not null, \
       SCHOOL_KIND    varchar(2) not null, \
       CURRICULUM_CD  varchar(2) not null, \
       SUBCLASSCD     varchar(6) not null, \
       SCHREGNO       varchar(8) not null, \
       SEQ            smallint not null, \
       DSEQ           varchar(3) NOT NULL, \
       REMARK1        varchar(300),  \
       REMARK2        varchar(300),  \
       REMARK3        varchar(300),  \
       REMARK4        varchar(300),  \
       REMARK5        varchar(300),  \
       REGISTERCD     varchar(8), \
       UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table RECORD_SCORE_HIST_DETAIL_DAT add constraint PK_SCORE_HIST_DET \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO,SEQ,DSEQ)


insert into RECORD_SCORE_HIST_DETAIL_DAT select * from RECORD_SCORE_HIST_DETAIL_DAT_OLD
