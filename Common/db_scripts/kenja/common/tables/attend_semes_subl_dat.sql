-- kanji=����
-- $Id: 4464a95efd2140e7b30d674e1a0283b897ea53d8 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEND_SEMES_SUBL_DAT

create table ATTEND_SEMES_SUBL_DAT \
        (COPYCD             varchar(1)      not null, \
         YEAR               varchar(4)      not null, \
         MONTH              varchar(2)      not null, \
         SEMESTER           varchar(1)      not null, \
         SCHREGNO           varchar(8)      not null, \
         DI_CD              varchar(2)      not null, \
         SUBL_CD            varchar(3)      not null, \
         CNT                smallint, \
         REGISTERCD         varchar(8), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_SEMES_SUBL_DAT add constraint pk_at_sem_subl_dat primary key \
        (COPYCD, YEAR, MONTH, SEMESTER, SCHREGNO, DI_CD, SUBL_CD)


