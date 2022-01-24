-- kanji=����
-- $Id: levy_request_income_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ��ǡ���

drop table LEVY_REQUEST_INCOME_DAT

create table LEVY_REQUEST_INCOME_DAT \
( \
        "YEAR"                      varchar(4)  not null, \
        "INCOME_L_CD"               varchar(2)  not null, \
        "INCOME_M_CD"               varchar(2)  not null, \
        "REQUEST_NO"                varchar(10) not null, \
        "REQUEST_DATE"              date, \
        "REQUEST_REASON"            varchar(120), \
        "REQUEST_STAFF"             varchar(10), \
        "REQUEST_GK"                int, \
        "COLLECT_L_CD"              varchar(2), \
        "COLLECT_M_CD"              varchar(2), \
        "COLLECT_S_CD"              varchar(2), \
        "INCOME_APPROVAL"           varchar(1), \
        "INCOME_CANCEL"             varchar(1), \
        "INCOME_DIV"                varchar(2), \
        "INCOME_DATE"               date, \
        "INCOME_NO"                 varchar(10), \
        "INCOME_STAFF"              varchar(10), \
        "INCOME_CERTIFICATE_CNT"    varchar(10), \
        "REGISTERCD"                varchar(10), \
        "UPDATED"                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_DAT add constraint PK_LEVY_REQ_INCOME primary key (YEAR, INCOME_L_CD, INCOME_M_CD, REQUEST_NO)
