-- kanji=漢字
-- $Id: virtual_bank_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table VIRTUAL_BANK_MST

create table VIRTUAL_BANK_MST \
(  \
    VIRTUAL_BANK_CD varchar(3) not null, \
    BANK_CD         varchar(4) not null, \
    BRANCH_CD       varchar(3) not null, \
    BANK_NAME       varchar(45), \
    BANK_KANA       varchar(45), \
    BRANCH_NAME     varchar(45), \
    BRANCH_KANA     varchar(45), \
    BANK_ZIPCD      varchar(8), \
    BANK_ADDR1      varchar(75), \
    BANK_ADDR2      varchar(75), \
    BANK_TELNO      varchar(14), \
    BANK_FAXNO      varchar(14), \
    ACCOUNT_NAME    varchar(90), \
    ACCOUNT_KANA    varchar(120), \
    ACCOUNT_ZIPCD   varchar(8), \
    ACCOUNT_ADDR1   varchar(75), \
    ACCOUNT_ADDR2   varchar(75), \
    ACCOUNT_ADDR3   varchar(75), \
    ACCOUNT_TELNO   varchar(14), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table VIRTUAL_BANK_MST  \
add constraint PK_VIRTUAL_BANK_M \
primary key  \
(VIRTUAL_BANK_CD)
