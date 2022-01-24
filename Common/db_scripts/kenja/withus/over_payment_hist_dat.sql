-- kanji=漢字
-- $Id: over_payment_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table OVER_PAYMENT_HIST_DAT

create table OVER_PAYMENT_HIST_DAT \
    (APPLICANTNO        varchar(7) not null, \
     OVER_PAY_DATE      date not null, \
     OVER_PAYMENT_DIV   varchar(2) not null, \
     INQUIRY_NO         varchar(6) not null, \
     PAYMENT_DIV        varchar(2) not null, \
     PAYMENT_INQUIRY_NO varchar(6) not null, \
     OVER_PAYMENT       integer, \
     REGISTERCD         varchar(8)  , \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table OVER_PAYMENT_HIST_DAT add constraint PK_OVER_PAY_DAT primary key \
      (APPLICANTNO, OVER_PAY_DATE, OVER_PAYMENT_DIV, INQUIRY_NO)
