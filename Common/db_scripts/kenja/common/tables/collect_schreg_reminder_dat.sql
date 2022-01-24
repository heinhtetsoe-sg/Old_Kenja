-- kanji=����
-- $Id$

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--����ͽ����ʬ��ǡ���
drop table COLLECT_SCHREG_REMINDER_DAT

create table COLLECT_SCHREG_REMINDER_DAT \
( \
    YEAR                    varchar(4)  not null, \
    SCHREGNO                varchar(8)  not null, \
    SEQ                     varchar(2)  not null, \
    REMINDER_COUNT          integer,     \
    DOCUMENTCD              varchar(2),  \
    REMINDER_MONEY          integer,     \
    REMINDER_STAFFCD        varchar(10), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SCHREG_REMINDER_DAT \
add constraint PK_CO_SCH_REMI_D \
primary key \
(YEAR, SCHREGNO, SEQ)
