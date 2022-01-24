-- kanji=漢字
-- $Id: collect_grp_schreg_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目グループHRクラスデータ
drop table COLLECT_GRP_SCHREG_DAT

create table COLLECT_GRP_SCHREG_DAT \
( \
        "YEAR"            varchar(4) not null, \
        "COLLECT_GRP_CD"  varchar(4) not null, \
        "SCHREGNO"        varchar(8) not null, \
        "REGISTERCD"      varchar(10), \
        "UPDATED"         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_GRP_SCHREG_DAT add constraint PK_COL_GRP_SCH_DAT primary key (YEAR, COLLECT_GRP_CD, SCHREGNO)
