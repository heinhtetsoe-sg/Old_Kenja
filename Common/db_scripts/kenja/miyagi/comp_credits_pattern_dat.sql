-- kanji=漢字
-- $Id: comp_credits_pattern_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table COMP_CREDITS_PATTERN_DAT

create table COMP_CREDITS_PATTERN_DAT \
(  \
        "YEAR"                  varchar(4) not null, \
        "PATTERN_CD"            varchar(2) not null, \
        "CLASSCD"               varchar(2) not null, \
        "SCHOOL_KIND"           varchar(2) not null, \
        "CURRICULUM_CD"         varchar(2) not null, \
        "SUBCLASSCD"            varchar(6) not null, \
        "COMP_FLG"              varchar(1), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMP_CREDITS_PATTERN_DAT  \
add constraint PK_COMP_CREDIT_P_D \
primary key  \
(YEAR, PATTERN_CD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
