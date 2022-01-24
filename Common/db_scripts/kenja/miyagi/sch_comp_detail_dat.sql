-- kanji=漢字
-- $Id: sch_comp_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SCH_COMP_DETAIL_DAT

create table SCH_COMP_DETAIL_DAT \
(  \
        "YEAR"                  varchar(4) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "CLASSCD"               varchar(2) not null, \
        "SCHOOL_KIND"           varchar(2) not null, \
        "CURRICULUM_CD"         varchar(2) not null, \
        "SUBCLASSCD"            varchar(6) not null, \
        "KOUNIN"                varchar(1), \
        "ADD_CREDIT"            smallint, \
        "YOBI1"                 varchar(60), \
        "YOBI2"                 varchar(60), \
        "YOBI3"                 varchar(60), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCH_COMP_DETAIL_DAT  \
add constraint PK_SCH_COMP_DETAIL \
primary key  \
(YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
