-- kanji=漢字
-- $Id: bc6a29b3cbb879557ee99a1eee08308d690f1d37 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SCH_PTRN_STF_DAT

create table SCH_PTRN_STF_DAT \
      (YEAR               varchar(4)   not null, \
       SEMESTER           varchar(1)   not null, \
       BSCSEQ             smallint     not null, \
       DAYCD              varchar(1)   not null, \
       PERIODCD           varchar(1)   not null, \
       CHAIRCD            varchar(7)   not null, \
       STAFFCD            varchar(8)   not null, \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCH_PTRN_STF_DAT add constraint pk_sch_ptrn_stf \
      primary key (YEAR, SEMESTER, BSCSEQ, DAYCD, PERIODCD, CHAIRCD, STAFFCD)
