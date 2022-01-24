-- kanji=漢字
-- $Id: month_no_sales_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MONTH_NO_SALES_DAT

create table MONTH_NO_SALES_DAT \
    (APPLICANTNO                    varchar(7) not null, \
     YEAR_MONTH                     varchar(6) not null, \
     LAST_MONTH_NO_SALES_MONEY      integer, \
     LAST_MONTH_KEEPING_MONEY       integer, \
     MONTH_PAYMENT_MONEY            integer, \
     MONTH_APPROPRIATED_MONEY       integer, \
     MONTH_APPROPRIATED_MONEY_DISP  integer, \
     MONTH_SALES_MONEY              integer, \
     MONTH_KEEPING_MONEY            integer, \
     MONTH_NO_SALES_MONEY           integer, \
     MONTH_NO_SALES_MONEY_DISP      integer, \
     TOTAL_NO_SALES_MONEY           integer, \
     TOTAL_NO_SALES_MONEY_DISP      integer, \
     REGISTERCD                     varchar(8), \
     UPDATED                        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MONTH_NO_SALES_DAT add constraint PK_MONTH_NO_SALES primary key \
      (APPLICANTNO, YEAR_MONTH)
