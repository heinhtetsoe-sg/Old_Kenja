-- kanji=漢字
-- $Id: dcb6233c21269980cd427df28d59b6724bd0de0e $
-- 学籍在籍データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--


drop table ATTEND_SEMES_LESSON_DAT

create table ATTEND_SEMES_LESSON_DAT \
      (YEAR             varchar(4) not null, \
       MONTH            varchar(2) not null, \
       SEMESTER         varchar(1) not null, \
       GRADE            varchar(2) not null, \
       HR_CLASS         varchar(3) not null, \
       LESSON           smallint not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table ATTEND_SEMES_LESSON_DAT add constraint PK_AT_SE_LESSON_D primary key (YEAR, MONTH, SEMESTER, GRADE, HR_CLASS)
