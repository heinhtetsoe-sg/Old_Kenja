-- kanji=漢字
-- $Id: ebce81b85c554361030ba411a758dbd693c4a2bf $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table UNIT_SCH_CHR_BUNKATU_DAT

create table UNIT_SCH_CHR_BUNKATU_DAT \
      (EXECUTEDATE        date         not null, \
       PERIODCD           varchar(1)   not null, \
       MAIN_CHAIRCD       varchar(7)   not null, \
       SEQ                smallint     not null, \
       CHAIRCD            varchar(7)   not null, \
       MINUTE             smallint     not null, \
       YEAR               varchar(4), \
       SEMESTER           varchar(1), \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table UNIT_SCH_CHR_BUNKATU_DAT add constraint PK_UNIT_SCH_CR_DAT primary key \
      (EXECUTEDATE, PERIODCD, MAIN_CHAIRCD, SEQ)
