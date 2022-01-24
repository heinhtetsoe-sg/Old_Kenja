-- kanji=����
-- $Id: attest_inkan_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEST_INKAN_DAT

create table ATTEST_INKAN_DAT \
(  \
        "STAMP_NO"         varchar(6) not null, \
        "STAFFCD"          varchar(8) not null, \
        "DIST"             varchar(1), \
        "DATE"             date, \
        "START_DATE"       date, \
        "STOP_DATE"        date, \
        "START_REASON"     varchar(30), \
        "STOP_REASON"      varchar(30), \
        "REGISTERCD"       varchar(8), \
        "UPDATED"          timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ATTEST_INKAN_DAT  \
add constraint PK_INKAN_DAT  \
primary key  \
(STAMP_NO)
