-- kanji=����
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--


drop table SCHREG_IQ_DAT \

create table SCHREG_IQ_DAT \
      (YEAR              varchar(4) not null, \
       SCHREGNO          varchar(8) not null, \
       IQ                smallint, \
       REGISTERCD        varchar(10), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_IQ_DAT add constraint PK_SCHREG_IQ_DAT primary key (YEAR, SCHREGNO)
