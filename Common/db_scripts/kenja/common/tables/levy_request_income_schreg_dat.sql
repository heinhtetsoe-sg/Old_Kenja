-- kanji=����
-- $Id: 9c2be3f17d6814cf48b74dc088d3f3bedd74d867 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ����̥ǡ���

drop table LEVY_REQUEST_INCOME_SCHREG_DAT

create table LEVY_REQUEST_INCOME_SCHREG_DAT \
( \
        "SCHOOLCD"                  varchar(12) not null, \
        "SCHOOL_KIND"               varchar(2)  not null, \
        "YEAR"                      varchar(4)  not null, \
        "INCOME_L_CD"               varchar(2)  not null, \
        "INCOME_M_CD"               varchar(2)  not null, \
        "REQUEST_NO"                varchar(10) not null, \
        "INCOME_S_CD"               varchar(3)  not null, \
        "SCHREGNO"                  varchar(8)  not null, \
        "LINE_NO"                   smallint    not null, \
        "INCOME_DATE"               date, \
        "INCOME_NO"                 varchar(10), \
        "INCOME_MONEY"              integer, \
        "INCOME_CERTIFICATE_CNT"    varchar(10), \
        "REGISTERCD"                varchar(10), \
        "UPDATED"                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_SCHREG_DAT add constraint PK_LEVY_REQIN_SC primary key (SCHOOLCD, SCHOOL_KIND, YEAR, INCOME_L_CD, INCOME_M_CD, REQUEST_NO, INCOME_S_CD, SCHREGNO)
