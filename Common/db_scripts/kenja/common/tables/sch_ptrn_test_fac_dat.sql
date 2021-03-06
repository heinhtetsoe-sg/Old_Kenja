-- kanji=漢字
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SCH_PTRN_TEST_FAC_DAT

create table SCH_PTRN_TEST_FAC_DAT \
      (YEAR               varchar(4)   not null, \
       SEMESTER           varchar(1)   not null, \
       BSCSEQ             smallint     not null, \
       DAYCD              varchar(1)   not null, \
       PERIODCD           varchar(1)   not null, \
       CHAIRCD            varchar(7)   not null, \
       FACCD              varchar(4)   not null, \
       REGISTERCD         varchar(10), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCH_PTRN_TEST_FAC_DAT add constraint PK_SCH_PTRN_TEST_FAC_DAT \
      primary key (YEAR, SEMESTER, BSCSEQ, DAYCD, PERIODCD, CHAIRCD, FACCD)
