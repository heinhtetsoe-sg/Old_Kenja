-- kanji=����
-- $Id: fe46a06f06c2a92f3e11637760fbb03a988cf729 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table BEHAVIOR_SEMES_MST

create table BEHAVIOR_SEMES_MST \
  (YEAR                 varchar(4) not null, \
   GRADE                varchar(2) not null, \
   CODE                 varchar(2) not null, \
   CODENAME             varchar(45), \
   VIEWNAME             varchar(210), \
   STUDYREC_CODE        varchar(2), \
   REGISTERCD           varchar(10), \
   UPDATED              timestamp default current timestamp \
  ) in usr1dms index in idx1dms

alter table BEHAVIOR_SEMES_MST \
add constraint PK_BEHAVIOR_SEM_M \
primary key \
(YEAR, GRADE, CODE)

