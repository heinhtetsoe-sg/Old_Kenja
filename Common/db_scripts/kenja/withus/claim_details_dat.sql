-- kanji=漢字
-- $Id: claim_details_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLAIM_DETAILS_DAT

create table CLAIM_DETAILS_DAT \
(  \
        SLIP_NO           varchar(8) not null, \
        SEQ               varchar(2) not null, \
        APPLICANTNO       varchar(7) not null, \
        COMMODITY_CD      varchar(5) not null, \
        AMOUNT            varchar(2), \
        CLAIM_DATE        date, \
        TOTAL_CLAIM_MONEY integer, \
        PRICE             integer, \
        TOTAL_PRICE       integer, \
        TAX               integer, \
        S_YEAR_MONTH      varchar(6) not null, \
        E_YEAR_MONTH      varchar(6) not null, \
        PRIORITY_LEVEL    varchar(2), \
        PAYMENT_MONEY     integer, \
        PAYMENT_DATE      date, \
        SUMMING_UP_MONEY  integer, \
        SUMMING_UP_DATE   date, \
        DUMMY_FLG         varchar(1), \
        REMARK            varchar(150), \
        REGISTERCD        varchar(8), \
        UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLAIM_DETAILS_DAT  \
add constraint PK_CLAIM_DETAILS  \
primary key  \
(SLIP_NO, SEQ)
