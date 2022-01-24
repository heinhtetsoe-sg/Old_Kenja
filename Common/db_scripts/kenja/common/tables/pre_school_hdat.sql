-- kanji=漢字
-- $Id: e52282487fc863185cf450d4f8184e7fec663b40 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table PRE_SCHOOL_HDAT

create table PRE_SCHOOL_HDAT \
      (YEAR         varchar(4)   not null, \
       SEMESTER     varchar(1)   not null, \
       GRADE        varchar(2)   not null, \
       HR_CLASS     varchar(3)   not null, \
       HR_NAME      varchar(15), \
       HR_NAMEABBV  varchar(5), \
       REGISTERCD   varchar(8), \
       UPDATED      timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table PRE_SCHOOL_HDAT add constraint PK_PRE_SCHOOL_HDAT primary key (YEAR, SEMESTER, GRADE, HR_CLASS)

