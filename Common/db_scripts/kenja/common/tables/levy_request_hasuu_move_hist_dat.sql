-- kanji=����
-- $Id: 7b6b3da5729e0cda02edf71ee762d67aa152766e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ü����ư����ǡ���

drop table LEVY_REQUEST_HASUU_MOVE_HIST_DAT

create table LEVY_REQUEST_HASUU_MOVE_HIST_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "FROM_REQUEST_NO"       varchar(10) not null, \
        "FROM_LINE_NO"          smallint    not null, \
        "FROM_OUTGO_L_CD"       varchar(2)  not null, \
        "FROM_OUTGO_M_CD"       varchar(2)  not null, \
        "FROM_OUTGO_S_CD"       varchar(3)  not null, \
        "TO_REQUEST_NO"         varchar(10) not null, \
        "TO_LINE_NO"            smallint    not null, \
        "TO_OUTGO_L_CD"         varchar(2)  not null, \
        "TO_OUTGO_M_CD"         varchar(2)  not null, \
        "TO_OUTGO_S_CD"         varchar(3)  not null, \
        "MOVE_HASUU"            integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms
alter table LEVY_REQUEST_HASUU_MOVE_HIST_DAT add constraint PK_LEVY_HASUU_MOV \
primary key (SCHOOLCD, SCHOOL_KIND, YEAR, FROM_REQUEST_NO, FROM_LINE_NO, FROM_OUTGO_L_CD, FROM_OUTGO_M_CD, FROM_OUTGO_S_CD, TO_REQUEST_NO, TO_LINE_NO, TO_OUTGO_L_CD, TO_OUTGO_M_CD, TO_OUTGO_S_CD)
