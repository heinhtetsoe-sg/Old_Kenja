-- kanji=����
-- $Id: 681236b65080484eca8ea1c0aa746a59a83c2974 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ����٥ǡ���

drop table LEVY_REQUEST_KOUNYU_MEISAI_DAT_OLD
create table LEVY_REQUEST_KOUNYU_MEISAI_DAT_OLD like LEVY_REQUEST_KOUNYU_MEISAI_DAT
insert into LEVY_REQUEST_KOUNYU_MEISAI_DAT_OLD select * from LEVY_REQUEST_KOUNYU_MEISAI_DAT

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

insert into LEVY_REQUEST_KOUNYU_MEISAI_DAT \
select \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    REQUEST_NO, \
    LINE_NO, \
    KOUNYU_L_CD, \
    KOUNYU_M_CD, \
    KOUNYU_S_CD, \
    COMMODITY_PRICE, \
    COMMODITY_CNT, \
    TOTAL_PRICE_ZEINUKI, \
    TOTAL_TAX, \
    TOTAL_PRICE, \
    REMARK, \
    cast(null as integer) as SCH_PRICE, \
    cast(null as integer) as SCH_CNT, \
    cast(null as integer) as HASUU, \
    REGISTERCD, \
    UPDATED \
from LEVY_REQUEST_KOUNYU_MEISAI_DAT_OLD
