-- kanji=漢字
-- $Id: d9d9f4c27e1eeaf263cf60a0e992150cd1d013d6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SCH_STF_DAT

create table SCH_STF_DAT \
      (EXECUTEDATE        date         not null, \
       PERIODCD           varchar(1)   not null, \
       CHAIRCD            varchar(7)   not null, \
       STAFFCD            varchar(8)   not null, \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCH_STF_DAT add constraint pk_sch_stf_dat \
      primary key (EXECUTEDATE, PERIODCD, CHAIRCD, STAFFCD)
