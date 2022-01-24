-- kanji=����
-- $Id: attest_opinions_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEST_OPINIONS_DAT

create table ATTEST_OPINIONS_DAT \
(  \
        "YEAR"             varchar(4) not null, \
        "SEQ"              integer not null, \
        "STAFFCD"          varchar(8) not null, \
        "CERT_NO"          integer not null, \
        "OPINION"          varchar(300), \
        "SIGNATURE"        varchar(350) not null, \
        "RESULT"           integer not null, \
        "SCHREGNO"         varchar(8) not null, \
        "ACTION"           smallint not null, \
        "STATUS"           smallint not null, \
        "REGISTERCD"       varchar(8), \
        "UPDATED"          timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ATTEST_OPINIONS_DAT  \
add constraint PK_OPINIONS_DAT  \
primary key  \
(YEAR, SEQ)
