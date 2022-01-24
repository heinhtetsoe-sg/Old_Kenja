-- kanji=����
-- $Id: dcb6233c21269980cd427df28d59b6724bd0de0e $
-- ���Һ��ҥǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--


drop table ATTEND_SEMES_LESSON_DAT

create table ATTEND_SEMES_LESSON_DAT \
      (YEAR             varchar(4) not null, \
       MONTH            varchar(2) not null, \
       SEMESTER         varchar(1) not null, \
       GRADE            varchar(2) not null, \
       HR_CLASS         varchar(3) not null, \
       LESSON           smallint not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table ATTEND_SEMES_LESSON_DAT add constraint PK_AT_SE_LESSON_D primary key (YEAR, MONTH, SEMESTER, GRADE, HR_CLASS)
