-- kanji=漢字
-- $Id: attest_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
