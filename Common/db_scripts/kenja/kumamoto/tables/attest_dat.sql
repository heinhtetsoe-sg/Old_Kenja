-- kanji=����
-- $Id: attest_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEST_DAT

create table ATTEST_DAT \
(  \
        "YEAR"             varchar(4) not null, \
        "SEQ"              integer not null, \
        "STAFFCD"          varchar(8) not null, \
        "CERT_NO"          integer not null, \
        "RANDOM"           varchar(20) not null, \
        "SIGNATURE"        varchar(350) not null, \
        "RESULT"           integer not null, \
        "REGISTERCD"       varchar(8), \
        "UPDATED"          timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ATTEST_DAT  \
add constraint PK_ATTEST_DAT  \
primary key  \
(YEAR, SEQ)
