-- kanji=漢字
-- $Id: a5526787c2d97aaaa11cbfd8449ff1097568dc70 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--学部学科賢者SIGEL変換テーブル

drop table COLLECT_SGL_COURSE_MAPPING_DAT

create table COLLECT_SGL_COURSE_MAPPING_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "GRADE"                 varchar(2)  not null, \
        "HR_CLASS"              varchar(3)  not null, \
        "SGL_SCHOOLKIND"        varchar(1)  , \
        "SGL_MAJORCD"           varchar(3)  , \
        "SGL_COURSECODE"        varchar(4)  , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_COURSE_MAPPING_DAT \
add constraint PK_C_SGL_COU_MAPD \
primary key \
(YEAR, GRADE, HR_CLASS)
