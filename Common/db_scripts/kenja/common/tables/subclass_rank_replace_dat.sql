-- kanji=漢字
-- $Id: fbd86ad14b02ffaf41acd057977e49b1c73bfa61 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SUBCLASS_RANK_REPLACE_DAT

create table SUBCLASS_RANK_REPLACE_DAT ( \
    YEAR                      varchar(4) not null, \
    COMBINED_CLASSCD          varchar(2) not null, \
    COMBINED_SCHOOL_KIND      varchar(2) not null, \
    COMBINED_CURRICULUM_CD    varchar(2) not null, \
    COMBINED_SUBCLASSCD       varchar(6) not null, \
    ATTEND_CLASSCD            varchar(2) not null, \
    ATTEND_SCHOOL_KIND        varchar(2) not null, \
    ATTEND_CURRICULUM_CD      varchar(2) not null, \
    ATTEND_SUBCLASSCD         varchar(6) not null, \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_RANK_REPLACE_DAT add constraint PK_SUBRANKREP_DAT \
        primary key (YEAR, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
