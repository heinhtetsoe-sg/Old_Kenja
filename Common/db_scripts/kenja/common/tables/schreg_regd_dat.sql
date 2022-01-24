-- kanji=漢字
-- $Id: 8ed4b697f2cff541fb3b58728ec86c6952ebf073 $
-- 学籍在籍データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--


drop table SCHREG_REGD_DAT \

create table SCHREG_REGD_DAT \
      (SCHREGNO          varchar(8)      not null, \
       YEAR              varchar(4)      not null, \
       SEMESTER          varchar(1)      not null, \
       GRADE             varchar(2), \
       HR_CLASS          varchar(3), \
       ATTENDNO          varchar(3), \
       ANNUAL            varchar(2), \
       SEAT_ROW          varchar(2), \
       SEAT_COL          varchar(2), \
       COURSECD          varchar(1), \
       MAJORCD           varchar(3), \
       COURSECODE        varchar(4), \
       REGISTERCD        varchar(8), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_DAT add constraint PK_SCHREG_REGD_DAT primary key (SCHREGNO, YEAR, SEMESTER)
