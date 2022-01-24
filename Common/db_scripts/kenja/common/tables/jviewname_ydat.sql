-- kanji=漢字
-- $Id: dc961246725d2025c73657d30ad7c91dfbb1d00e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table JVIEWNAME_YDAT

create table JVIEWNAME_YDAT  \
(  \
        "YEAR"          varchar(4)  not null, \
        "VIEWCD"        varchar(4)  not null, \
        "REGISTERCD"    varchar(8),  \
        "UPDATED"       timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table JVIEWNAME_YDAT  \
add constraint pk_jviewname_ydat  \
primary key  \
( \
YEAR, \
VIEWCD \
)

