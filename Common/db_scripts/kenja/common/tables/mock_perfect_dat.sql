-- kanji=����
-- $Id: 28eddd7218dbeaf4c5f041626d36cc74c2faa585 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MOCK_PERFECT_DAT

create table MOCK_PERFECT_DAT \
      (YEAR             varchar(4) not null, \
       COURSE_DIV       varchar(1) not null, \
       GRADE            varchar(2) not null, \
       MOCK_SUBCLASS_CD varchar(6) not null, \
       PERFECT          smallint not null, \
       PASS_SCORE       smallint, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MOCK_PERFECT_DAT add constraint pk_mock_perfect_d \
      primary key (YEAR, COURSE_DIV, GRADE, MOCK_SUBCLASS_CD)
