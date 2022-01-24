-- kanji=漢字
-- $Id: 67c2b0a41f2042307e229933a26ceb8bba7b3fe4 $
-- 学籍在籍データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--


drop table SCHREG_REGD_GDAT

create table SCHREG_REGD_GDAT \
      (YEAR         varchar(4) not null, \
       GRADE        varchar(2) not null, \
       SCHOOL_KIND  varchar(2) not null, \
       GRADE_CD     varchar(2) not null, \
       GRADE_NAME1  varchar(60) not null, \
       GRADE_NAME2  varchar(60), \
       GRADE_NAME3  varchar(60), \
       REGISTERCD   varchar(8), \
       UPDATED      timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_GDAT add constraint PK_SCHREG_REGD_G primary key (YEAR, GRADE)
