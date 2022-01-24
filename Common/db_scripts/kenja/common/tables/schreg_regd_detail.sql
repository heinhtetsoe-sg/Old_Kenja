-- kanji=漢字
-- $Id: a381617a74c495d9078f1ca3f7790e39f8b0b5e8 $
-- 学籍在籍データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--


drop table SCHREG_REGD_DETAIL

create table SCHREG_REGD_DETAIL \
      (SCHREGNO          varchar(8)      not null, \
       YEAR              varchar(4)      not null, \
       SEMESTER          varchar(1)      not null, \
       COUNTFLG          varchar(1), \
       REGISTERCD        varchar(8), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_DETAIL add constraint PK_SRG_RGD_DTL primary key (SCHREGNO, YEAR, SEMESTER)
