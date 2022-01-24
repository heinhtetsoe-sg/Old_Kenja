-- kanji=����
-- $Id: 7b8cc454908c8957977b57ba816cab2fd6b10ffd $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_SCORE_DETAIL_DAT

create table RECORD_SCORE_DETAIL_DAT \
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
       DETAIL_SEQ     varchar(3) not null, \
       INT_VAL1       integer, \
       INT_VAL2       integer, \
       INT_VAL3       integer, \
       STR_VAL1       varchar(2), \
       STR_VAL2       varchar(2), \
       STR_VAL3       varchar(2), \
       DEC_VAL1       decimal(8,5), \
       DEC_VAL2       decimal(8,5), \
       DEC_VAL3       decimal(8,5), \
       REGISTERCD     varchar(10), \
       UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table RECORD_SCORE_DETAIL_DAT add constraint PK_REC_SCORE_DET \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO,DETAIL_SEQ)
