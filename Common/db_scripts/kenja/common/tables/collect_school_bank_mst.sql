-- kanji=漢字
-- $Id: 13a0966f8ffd734db99bb68ac35d78fbdb9f9c0c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table COLLECT_SCHOOL_BANK_MST

create table COLLECT_SCHOOL_BANK_MST \
( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    YEAR                varchar(4)  not null, \
    BANK_CD             varchar(4)  not null, \
    FORMAT_DIV          varchar(1)  not null, \
    SEQ                 varchar(3)  not null, \
    SHUBETSU_CD         varchar(2), \
    CODE_DIV            varchar(1), \
    ACCOUNT_CD          varchar(10), \
    ACCOUNTNAME_KANA    varchar(120), \
    BANKNAME_KANA       varchar(45), \
    BRANCHCD            varchar(3), \
    BRANCHNAME_KANA     varchar(45), \
    DEPOSIT_TYPE        varchar(1), \
    ACCOUNTNO           varchar(7), \
    SCHOOLZIPCD         varchar(8), \
    SCHOOLADDR1         varchar(150), \
    SCHOOLADDR2         varchar(150), \
    SCHOOLTELNO         varchar(14), \
    SCHOOLFAXNO         varchar(14), \
    BANK_TRANSFER_FEE   smallint, \
    JC_CD               varchar(2), \
    TARGET_BANK_DIV     varchar(1), \
    TARGET_BANK_CD      varchar(4), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SCHOOL_BANK_MST \
add constraint PK_COLL_SCH_BAN_M \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, BANK_CD, FORMAT_DIV, SEQ)
