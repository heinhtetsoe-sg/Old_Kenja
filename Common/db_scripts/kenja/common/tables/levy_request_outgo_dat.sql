-- kanji=����
-- $Id: 35722ca551ae2e26791114723ecb08bd89dd0b27 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����ٽлǤ��ǡ���

drop table LEVY_REQUEST_OUTGO_DAT

create table LEVY_REQUEST_OUTGO_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "INCOME_L_CD"           varchar(2)  not null, \
        "INCOME_M_CD"           varchar(2)  not null, \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "TRADER_CD"             varchar(8), \
        "TRADER_NAME"           varchar(120), \
        "BANKCD"                varchar(4), \
        "BRANCHCD"              varchar(3), \
        "BANK_DEPOSIT_ITEM"     varchar(1), \
        "BANK_ACCOUNTNO"        varchar(7), \
        "ACCOUNTNAME"           varchar(120), \
        "ACCOUNTNAME_KANA"      varchar(120), \
        "PAY_DIV"               varchar(2), \
        "OUTGO_CHECK1"          varchar(1), \
        "OUTGO_CHECK1_DATE"     date, \
        "OUTGO_CHECK1_STAFF"    varchar(10), \
        "OUTGO_CHECK2"          varchar(1), \
        "OUTGO_CHECK3"          varchar(1), \
        "OUTGO_DATE"            date, \
        "OUTGO_EXPENSE_FLG"     varchar(1), \
        "OUTGO_CERTIFICATE_CNT" smallint, \
        "OUTGO_CANCEL"          varchar(1), \
        "OUTGO_APPROVAL"        varchar(1), \
        "KOUNYU_NO"             varchar(10), \
        "SEKOU_NO"              varchar(10), \
        "SEISAN_NO"             varchar(10), \
        "HENKIN_FLG"            varchar(1), \
        "HENKIN_APPROVAL"       varchar(1), \
        "HENKIN_DATE"           date, \
        "COLLECT_GRP_CD"        varchar(4), \
        "COLLECT_L_CD"          varchar(2), \
        "COLLECT_M_CD"          varchar(2), \
        "COLLECT_S_CD"          varchar(3), \
        "COLLECT_DIV"           varchar(1), \
        "FROM_SCHOOL_KIND"      varchar(2), \
        "TESUURYOU_SUMMARY"     varchar(30), \
        "REMARK"                varchar(200), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_OUTGO_DAT add constraint PK_LEVY_REQ_OUT primary key (SCHOOLCD, SCHOOL_KIND, YEAR, OUTGO_L_CD, OUTGO_M_CD, REQUEST_NO)
