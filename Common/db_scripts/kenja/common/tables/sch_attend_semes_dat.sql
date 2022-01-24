-- kanji=漢字
-- $Id: 98144acd80baccf490dfa22226c4eb2239beec30 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table SCH_ATTEND_SEMES_DAT

create table SCH_ATTEND_SEMES_DAT \
    (YEAR               varchar(4) not null, \
     SEMESTER           varchar(1) not null, \
     SCHREGNO           varchar(8) not null, \
     CHAIRCD            varchar(7) not null, \
     SCHOOLING_CNT      smallint, \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) IN USR1DMS INDEX IN IDX1DMS

alter table SCH_ATTEND_SEMES_DAT add constraint PK_SCH_ATTEND_SEM primary key (YEAR, SEMESTER, SCHREGNO, CHAIRCD)
