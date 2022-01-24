-- kanji=漢字
-- $Id: claim_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLAIM_DAT

create table CLAIM_DAT \
(  \
            SLIP_NO           varchar(8) not null, \
            APPLICANTNO       varchar(7) not null, \
            SLIP_DIV          varchar(1), \
            MANNER_PAYMENT    varchar(1), \
            PAYMENT_SEQ       varchar(2), \
            TOTAL_MONEY       integer, \
            CLAIM_DATE        date, \
            TOTAL_CLAIM_MONEY integer, \
            PRICE             integer, \
            TAX               integer, \
            PAYMENT_MONEY     integer, \
            PAYMENT_DATE      date, \
            SUMMING_UP_MONEY  integer, \
            SUMMING_UP_DATE   date, \
            AZCASHIN_FLG      varchar(1), \
            CANCEL_FLG        varchar(1), \
            TEMP_CREDITS      smallint, \
            COMP_ENT_FLG      varchar(1), \
            BATCH_FLG         varchar(1), \
            REGISTERCD        varchar(8), \
            UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLAIM_DAT  \
add constraint PK_CLAIM_DAT  \
primary key  \
(SLIP_NO)
