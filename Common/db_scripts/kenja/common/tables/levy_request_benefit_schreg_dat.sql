-- kanji=����
-- $Id: ceeb2ddd76ea3356601a059dd26831575e4b39a6 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����������̥ǡ���

drop table LEVY_REQUEST_BENEFIT_SCHREG_DAT

create table LEVY_REQUEST_BENEFIT_SCHREG_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "BENEFIT_MONEY"         integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_BENEFIT_SCHREG_DAT add constraint PK_LEVY_BENE_SCH primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO)
