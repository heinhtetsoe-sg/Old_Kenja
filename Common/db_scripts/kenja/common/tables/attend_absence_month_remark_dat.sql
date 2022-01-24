-- kanji=����
-- $Id: 53bdfde2456db3d9178844ee0dc7f6205a652082 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEND_ABSENCE_MONTH_REMARK_DAT

create table ATTEND_ABSENCE_MONTH_REMARK_DAT \
        (YEAR               varchar(4) not null, \
         MONTH              varchar(2) not null, \
         SEMESTER           varchar(1) not null, \
         SCHREGNO           varchar(8) not null, \
         SEQ                smallint   not null, \
         DI_CD              varchar(2),  \
         TOTAL_DAY          varchar(3),  \
         REMARK             varchar(60), \
         TREATMENT          varchar(90), \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_ABSENCE_MONTH_REMARK_DAT add constraint PK_AT_AB_MON_RE_D primary key \
        (YEAR, MONTH, SEMESTER, SCHREGNO, SEQ)
