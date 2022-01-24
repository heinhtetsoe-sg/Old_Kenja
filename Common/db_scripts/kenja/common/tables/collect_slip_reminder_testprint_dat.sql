-- kanji=����
-- $Id: 563ad37ccdf0662b7cebbe315ebe6bede16cdffa $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--����ͽ����ʬ��ǡ���
drop table COLLECT_SLIP_REMINDER_TESTPRINT_DAT

create table COLLECT_SLIP_REMINDER_TESTPRINT_DAT \
( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SLIP_NO                 varchar(15) not null, \
    SCHREGNO                varchar(8)  not null, \
    REMINDER_COUNT          integer,     \
    DOCUMENTCD              varchar(2),  \
    REMINDER_MONEY          integer,     \
    REMINDER_STAFFCD        varchar(10), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_REMINDER_TESTPRINT_DAT \
add constraint PK_CO_SLIP_REMIT \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO)
