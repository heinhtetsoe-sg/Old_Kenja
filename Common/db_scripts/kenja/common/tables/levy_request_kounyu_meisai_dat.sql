-- kanji=����
-- $Id: 09a54e8d295bd4ab8e1b4e6e731e8d60a642de14 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ����٥ǡ���

drop table LEVY_REQUEST_KOUNYU_MEISAI_DAT

create table LEVY_REQUEST_KOUNYU_MEISAI_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "LINE_NO"               smallint    not null, \
        "KOUNYU_L_CD"           varchar(2)  not null, \
        "KOUNYU_M_CD"           varchar(2)  not null, \
        "KOUNYU_S_CD"           varchar(2)  not null, \
        "COMMODITY_PRICE"       integer, \
        "COMMODITY_CNT"         integer, \
        "TOTAL_PRICE_ZEINUKI"   integer, \
        "TOTAL_TAX"             integer, \
        "TOTAL_PRICE"           integer, \
        "REMARK"                varchar(120), \
        "SCH_PRICE"             integer, \
        "SCH_CNT"               integer, \
        "HASUU"                 integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_KOUNYU_MEISAI_DAT add constraint PK_LEVY_KOUNYU_M primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REQUEST_NO, LINE_NO, KOUNYU_L_CD, KOUNYU_M_CD, KOUNYU_S_CD)
