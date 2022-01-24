-- kanji=����
-- $Id: 07e53feebce54335efdf0da849e43d6e6779ed14 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����ٽлǤ����٥ǡ���

DROP TABLE LEVY_REQUEST_OUTGO_MEISAI_DAT

create table LEVY_REQUEST_OUTGO_MEISAI_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "LINE_NO"               smallint    not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "OUTGO_S_CD"            varchar(2)  not null, \
        "COMMODITY_PRICE"       integer, \
        "COMMODITY_CNT"         integer, \
        "TOTAL_PRICE_ZEINUKI"   integer, \
        "TOTAL_TAX"             integer, \
        "TOTAL_PRICE"           integer, \
        "SCH_PRICE"             integer, \
        "SCH_CNT"               integer, \
        "HASUU"                 integer, \
        "WARIHURI_DIV"          varchar(1) not null, \
        "TRADER_SEIKYU_NO"      varchar(10), \
        "SEIKYU_MONTH"          varchar(2), \
        "REMARK"                varchar(120), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_OUTGO_MEISAI_DAT add constraint PK_LEVY_OUTGO_M PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR, REQUEST_NO, LINE_NO, OUTGO_L_CD, OUTGO_M_CD, OUTGO_S_CD)
