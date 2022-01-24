-- kanji=漢字
-- $Id: 80fc9b7e8f8126439ddd2e9a9fdb8e0c7cf8be38 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table JVIEWNAME_MST

create table JVIEWNAME_MST  \
(  \
        "VIEWCD"        varchar(4)  not null, \
        "VIEWNAME"      varchar(75), \
        "SHOWORDER"     smallint, \
        "REGISTERCD"    varchar(8),  \
        "UPDATED"       timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table JVIEWNAME_MST  \
add constraint pk_jviewname_mst  \
primary key  \
( \
VIEWCD \
)

