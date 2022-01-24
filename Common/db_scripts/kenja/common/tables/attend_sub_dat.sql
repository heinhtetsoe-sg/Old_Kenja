-- kanji=����
-- $Id: 2ab120d4f2e062092ceba9a624d82c5d8e9e3560 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEND_SUB_DAT

create table ATTEND_SUB_DAT \
        (SCHREGNO           varchar(8)      not null, \
         EXECUTEDATE        date            not null, \
         DI_CD              varchar(2)      not null, \
         SUBL_CD            varchar(3)      not null, \
         SUBM_CD            varchar(3), \
         REGISTERCD         varchar(8), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_SUB_DAT add constraint pk_ATTEND_SUB_DAT primary key \
        (SCHREGNO, EXECUTEDATE)
        