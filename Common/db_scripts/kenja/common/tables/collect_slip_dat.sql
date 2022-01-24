-- kanji=����
-- $Id: f3baf1a738f5e755748d04ddee593d971fd3185a $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��ɼ�ǡ���
DROP TABLE COLLECT_SLIP_DAT

CREATE TABLE COLLECT_SLIP_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SLIP_NO"               varchar(15) not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "PAY_DIV"               varchar(1),  \
        "SLIP_DATE"             date, \
        "SLIP_STAFFCD"          varchar(10), \
        "CANCEL_DATE"           date, \
        "CANCEL_STAFFCD"        varchar(10), \
        "CANCEL_REASON"         varchar(90), \
        "COLLECT_GRP_CD"        varchar(4)  not null, \
        "COLLECT_PATTERN_CD"    varchar(2)  not null, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_DAT \
add constraint PK_SLIP_DAT \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO)
