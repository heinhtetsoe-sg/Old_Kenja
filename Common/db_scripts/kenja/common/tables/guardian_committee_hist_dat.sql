-- kanji=漢字
-- $Id: b79ce31840c76af54181620202aee628f8791de8 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table GUARDIAN_COMMITTEE_HIST_DAT

create table GUARDIAN_COMMITTEE_HIST_DAT ( \
    YEAR               varchar(4) not null, \
    SCHREGNO           varchar(8) not null, \
    EXECUTIVECD        varchar(2) not null, \
    REMARK             varchar(60), \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GUARDIAN_COMMITTEE_HIST_DAT add constraint PK_G_COM_HIST_DAT primary key (YEAR, SCHREGNO)
