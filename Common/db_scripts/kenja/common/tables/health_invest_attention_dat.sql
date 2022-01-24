-- kanji=漢字
-- $Id: da3fa67a5b0d66af61fca5db305dce25ae60b105 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEALTH_INVEST_ATTENTION_DAT

create table HEALTH_INVEST_ATTENTION_DAT \
        (SCHREGNO               varchar(8)      not null, \
         ATTENTION              varchar(600), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_INVEST_ATTENTION_DAT add constraint pk_hea_inva_dat primary key \
        (SCHREGNO)
