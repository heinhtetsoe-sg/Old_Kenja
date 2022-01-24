-- kanji=漢字
-- $Id: sales_tightens_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SALES_TIGHTENS_HIST_DAT

create table SALES_TIGHTENS_HIST_DAT \
    (SALES_YEAR_MONTH    varchar(6) not null, \
     YEAR                varchar(4), \
     S_TIGHTENS_DATE     date, \
     E_TIGHTENS_DATE     date, \
     TEMP_TIGHTENS_FLAG  varchar(1), \
     REGISTERCD          varchar(8)  , \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SALES_TIGHTENS_HIST_DAT add constraint PK_SALES_TIGHTENS primary key \
      (SALES_YEAR_MONTH)
