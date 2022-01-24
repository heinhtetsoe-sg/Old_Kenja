-- kanji=漢字
-- $Id: c00e9725b326f2056a15a03696040eec9a499fc4 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table APPOINTED_DAY_GRADE_MST

create table APPOINTED_DAY_GRADE_MST \
      (YEAR             varchar(4) not null, \
       MONTH            varchar(2) not null, \
       SEMESTER         varchar(1) not null, \
       GRADE            varchar(2) not null, \
       APPOINTED_DAY    varchar(2) not null, \
       REGISTERCD       varchar(10), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table APPOINTED_DAY_GRADE_MST add constraint PK_APPOINTE_DAYG \
      primary key (YEAR, MONTH, SEMESTER, GRADE)