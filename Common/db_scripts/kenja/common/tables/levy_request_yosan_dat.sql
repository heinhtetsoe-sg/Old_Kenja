-- kanji=����
-- $Id: c52c6995bbe69f4eda30524e24acc0b88a1974e1 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����ܹ��Ǥ��ǡ���

drop table LEVY_REQUEST_YOSAN_DAT

create table LEVY_REQUEST_YOSAN_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "YOSAN_DIV"             varchar(2)  not null, \
        "YOSAN_L_CD"            varchar(2)  not null, \
        "YOSAN_M_CD"            varchar(2)  not null, \
        "REQUEST_NO"            varchar(10), \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "REMARK"                varchar(500), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_YOSAN_DAT add constraint PK_LEVY_YOSAN_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, YOSAN_DIV, YOSAN_L_CD, YOSAN_M_CD)
