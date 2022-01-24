-- kanji=漢字
-- $Id: 66e16f80143c39bd0f4b78fa851616546ebc70eb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop   table PRE_SCHOOL_INFO_DAT
create table PRE_SCHOOL_INFO_DAT \
      (YEAR         varchar(4)   not null, \
       EXAMNO       varchar(5)   not null, \
       SEMESTER     varchar(1)   not null, \
       GRADE        varchar(2)   not null, \
       HR_CLASS     varchar(3), \
       ATTENDNO     varchar(3), \
       PRE_HR_CLASS varchar(3), \
       PRE_ATTENDNO varchar(3), \
       ATTENDFLG1   varchar(1), \
       ATTENDFLG2   varchar(1), \
       SCORE1       smallint, \
       SCORE2       smallint, \
       TOTAL_SCORE  smallint, \
       STATIONCD1   varchar(1), \
       STATIONCD2   varchar(1), \
       STATIONCD3   varchar(1), \
       STATIONCD4   varchar(1), \
       PRE_INFO1    varchar(1), \
       PRE_INFO2    varchar(1), \
       PRE_INFO3    varchar(1), \
       REMARK       varchar(60), \
       REGISTERCD   varchar(8), \
       UPDATED      timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table PRE_SCHOOL_INFO_DAT add constraint PK_PRE_SCHOOL_INFO primary key (YEAR, EXAMNO)


