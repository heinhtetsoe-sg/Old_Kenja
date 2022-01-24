-- kanji=漢字
-- $Id: 3aa1f144c505967f09327934c467294e4638e1a0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--SIGEL学科マスタ

drop table COLLECT_SGL_COURSECODE_MST

create table COLLECT_SGL_COURSECODE_MST \
( \
        "SGL_SCHOOLKIND"        varchar(1)  not null, \
        "SGL_MAJORCD"           varchar(3)  not null, \
        "SGL_COURSECODE"        varchar(4)  not null, \
        "SGL_COURSECODE_NAME"   varchar(30) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_COURSECODE_MST \
add constraint PK_C_SGL_COCODE_M \
primary key \
(SGL_SCHOOLKIND, SGL_MAJORCD, SGL_COURSECODE)
