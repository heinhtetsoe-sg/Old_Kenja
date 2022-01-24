-- kanji=����
-- $Id: attest_opinions_wk.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEST_OPINIONS_WK

create table ATTEST_OPINIONS_WK \
(  \
        "YEAR"             varchar(4) not null, \
        "SCHREGNO"         varchar(8) not null, \
        "CHAGE_OPI_SEQ"    integer not null, \
        "CHAGE_STAFFCD"    varchar(8) not null, \
        "LAST_OPI_SEQ"     integer, \
        "LAST_STAFFCD"     varchar(8), \
        "REGISTERCD"       varchar(8), \
        "UPDATED"          timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ATTEST_OPINIONS_WK  \
add constraint PK_OPINIONS_WK  \
primary key  \
(YEAR, SCHREGNO)
