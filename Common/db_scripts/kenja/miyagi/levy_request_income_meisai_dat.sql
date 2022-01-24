-- kanji=����
-- $Id: levy_request_income_meisai_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ����٥ǡ���

drop table LEVY_REQUEST_INCOME_MEISAI_DAT

create table LEVY_REQUEST_INCOME_MEISAI_DAT \
( \
        "YEAR"                      varchar(4)  not null, \
        "INCOME_L_CD"               varchar(2)  not null, \
        "INCOME_M_CD"               varchar(2)  not null, \
        "REQUEST_NO"                varchar(10) not null, \
        "INCOME_S_CD"               varchar(2)  not null, \
        "LINE_NO"                   smallint    not null, \
        "COMMODITY_NAME"            varchar(120), \
        "COMMODITY_PRICE"           integer, \
        "COMMODITY_CNT"             integer, \
        "TOTAL_PRICE"               integer, \
        "WARIHURI_DIV"              varchar(1)  not null, \
        "REMARK"                    varchar(120), \
        "REGISTERCD"                varchar(10), \
        "UPDATED"                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_MEISAI_DAT add constraint PK_LEVY_REQIN_ME primary key (YEAR, INCOME_L_CD, INCOME_M_CD, REQUEST_NO, INCOME_S_CD)
