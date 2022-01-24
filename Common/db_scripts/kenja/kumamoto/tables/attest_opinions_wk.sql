-- kanji=漢字
-- $Id: attest_opinions_wk.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
