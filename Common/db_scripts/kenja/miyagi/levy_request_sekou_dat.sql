-- kanji=����
-- $Id: levy_request_sekou_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����ܹ��Ǥ��ǡ���

drop table LEVY_REQUEST_SEKOU_DAT

create table LEVY_REQUEST_SEKOU_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "SEKOU_L_CD"            varchar(2)  not null, \
        "SEKOU_M_CD"            varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "SEKOU_JIGYOU_NAME"     varchar(120), \
        "SEKOU_NAIYOU"          varchar(120), \
        "SEKOU_DATE_FROM"       date, \
        "SEKOU_DATE_TO"         date, \
        "SEKOU_PLACE"           varchar(120), \
        "KEIYAKU_HOUHOU"        varchar(120), \
        "REMARK"                varchar(500), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_SEKOU_DAT add constraint PK_LEVY_SEKOU_D primary key (YEAR, SEKOU_L_CD, SEKOU_M_CD, REQUEST_NO)
