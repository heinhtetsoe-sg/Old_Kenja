-- kanji=漢字
-- $Id: comp_credit_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table COMP_CREDIT_DAT

create table COMP_CREDIT_DAT \
(  \
    YEAR            varchar(4) not null, \
    APPLICANTNO     varchar(7) not null, \
    SLIP_NO         varchar(8) not null, \
    CREDIT_DIV      varchar(1) not null, \
    COMP_ENT_FLG    varchar(1), \
    COMP_CREDIT     smallint, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMP_CREDIT_DAT  \
add constraint PK_COMP_CREDIT_DAT \
primary key  \
(YEAR, APPLICANTNO, SLIP_NO, CREDIT_DIV)
