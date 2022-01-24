-- kanji=漢字
-- $Id: dd30636dffaab880b22fa503da0f62f767f58cb2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEALTH_RELA_DAT

create table HEALTH_RELA_DAT \
        (SCHREGNO               varchar(8)      not null, \
         RELANO                 varchar(2)      not null, \
         REMARK                 varchar(90), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_RELA_DAT add constraint pk_hea_rela_dat primary key \
        (SCHREGNO, RELANO)
