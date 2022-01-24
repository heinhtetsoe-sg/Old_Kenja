-- kanji=����
-- $Id: 46e9b452cbb8900a3d0bbef1abf38093f634ecea $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����������ǡ���

drop table LEVY_REQUEST_SEISAN_DAT

create table LEVY_REQUEST_SEISAN_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SEISAN_L_CD"           varchar(2)  not null, \
        "SEISAN_M_CD"           varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "SEISAN_TITLE"          varchar(120), \
        "GENKIN_JURYOU_STAFF"   varchar(10), \
        "SEISAN_NAIYOU"         varchar(250), \
        "JURYOU_GK"             integer, \
        "JURYOU_DATE"           date, \
        "SIHARAI_GK"            integer, \
        "SIHARAI_DATE"          date, \
        "ZAN_GK"                integer, \
        "REMARK"                varchar(250), \
        "SEISAN_APPROVAL"       varchar(1), \
        "SUITOU_STAFF"          varchar(10), \
        "INCOME_DATE"           date, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_SEISAN_DAT add constraint PK_LEVY_SEISAN_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SEISAN_L_CD, SEISAN_M_CD, REQUEST_NO)
