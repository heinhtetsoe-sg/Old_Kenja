-- kanji=����
-- $Id: 511db6a35886c78d3b8b6836a70a260eea61bc2d $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
-- ǯ����������ü����ɼ��ȥǡ���

drop table LEVY_REQUEST_HASUU_WORK_DAT

create table LEVY_REQUEST_HASUU_WORK_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "LINE_NO"               smallint    not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "OUTGO_S_CD"            varchar(3)  not null, \
        "INS_REQUEST_NO"        varchar(10) not null, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms
alter table LEVY_REQUEST_HASUU_WORK_DAT add constraint PK_LEVY_HASUU_WOR \
primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REQUEST_NO, LINE_NO, OUTGO_L_CD, OUTGO_M_CD, OUTGO_S_CD, INS_REQUEST_NO)
