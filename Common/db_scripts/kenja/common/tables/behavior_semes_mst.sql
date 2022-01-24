-- kanji=漢字
-- $Id: fe46a06f06c2a92f3e11637760fbb03a988cf729 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table BEHAVIOR_SEMES_MST

create table BEHAVIOR_SEMES_MST \
  (YEAR                 varchar(4) not null, \
   GRADE                varchar(2) not null, \
   CODE                 varchar(2) not null, \
   CODENAME             varchar(45), \
   VIEWNAME             varchar(210), \
   STUDYREC_CODE        varchar(2), \
   REGISTERCD           varchar(10), \
   UPDATED              timestamp default current timestamp \
  ) in usr1dms index in idx1dms

alter table BEHAVIOR_SEMES_MST \
add constraint PK_BEHAVIOR_SEM_M \
primary key \
(YEAR, GRADE, CODE)

