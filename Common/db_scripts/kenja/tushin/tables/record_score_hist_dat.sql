-- kanji=����
-- $Id: record_score_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_SCORE_HIST_DAT

create table RECORD_SCORE_HIST_DAT \
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
       TEST_DATE      date not null, \
       CHAIRCD        varchar(7), \
       SCORE          smallint, \
       VALUE          smallint, \
       VALUE_DI       varchar(2), \
       GET_CREDIT     smallint, \
       ADD_CREDIT     smallint, \
       COMP_TAKESEMES varchar(1), \
       COMP_CREDIT    smallint, \
       COMP_CONTINUE  varchar(1), \
       REGISTERCD     varchar(8), \
       UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table RECORD_SCORE_HIST_DAT add constraint pk_rec_score_hist \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO,SEQ)
