-- kanji=漢字
-- $Id: month_payment_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MONTH_PAYMENT_DAT

create table MONTH_PAYMENT_DAT \
    (YEAR_MONTH          varchar(6) not null, \
     DATA_DIV            varchar(1) not null, \
     STUDENT_DIV         varchar(2) not null, \
     COMMUTING_DIV       varchar(1), \
     PAYMENT_MONEY       integer, \
     PAYMENT_CNT         integer, \
     TOTAL_PAYMENT_MONEY integer, \
     TOTAL_PAYMENT_CNT   integer, \
     REGISTERCD          varchar(8)  , \
     UPDATED             timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MONTH_PAYMENT_DAT add constraint PK_MONTH_PAYMENT primary key \
      (YEAR_MONTH, DATA_DIV, STUDENT_DIV)
