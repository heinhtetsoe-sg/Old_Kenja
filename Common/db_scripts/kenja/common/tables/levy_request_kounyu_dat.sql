-- kanji=����
-- $Id: 354d39aae3f53e1ce53bbd1813f140acf61118e6 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ��ǡ���

drop table LEVY_REQUEST_KOUNYU_DAT

create table LEVY_REQUEST_KOUNYU_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "KOUNYU_L_CD"           varchar(2)  not null, \
        "KOUNYU_M_CD"           varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "TRADER_CD1"            varchar(8), \
        "TRADER_NAME1"          varchar(120), \
        "TRADER_KAKUTEI1"       varchar(1), \
        "TRADER_CD2"            varchar(8), \
        "TRADER_NAME2"          varchar(120), \
        "TRADER_KAKUTEI2"       varchar(1), \
        "TRADER_CD3"            varchar(8), \
        "TRADER_NAME3"          varchar(120), \
        "TRADER_KAKUTEI3"       varchar(1), \
        "TRADER_CD4"            varchar(8), \
        "TRADER_NAME4"          varchar(120), \
        "TRADER_KAKUTEI4"       varchar(1), \
        "KOUNYU_MITUMORI_DATE"  date, \
        "KEIYAKU_HOUHOU"        varchar(120), \
        "NOUNYU_LIMIT_DATE"     date, \
        "NOUNYU_PLACE"          varchar(120), \
        "REMARK"                varchar(120), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_KOUNYU_DAT add constraint PK_LEVY_KOUNYU primary key (SCHOOLCD, SCHOOL_KIND, YEAR, KOUNYU_L_CD, KOUNYU_M_CD, REQUEST_NO)
