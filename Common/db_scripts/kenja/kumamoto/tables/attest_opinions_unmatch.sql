-- kanji=����
-- $Id: attest_opinions_unmatch.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEST_OPINIONS_UNMATCH

create table ATTEST_OPINIONS_UNMATCH \
(  \
        "YEAR"             varchar(4) not null, \
        "SCHREGNO"         varchar(8) not null, \
        "FLG"              varchar(1) not null, \
        "REGISTERCD"       varchar(8), \
        "UPDATED"          timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ATTEST_OPINIONS_UNMATCH  \
add constraint PK_ATTEST_UNMATCH  \
primary key  \
(YEAR, SCHREGNO)