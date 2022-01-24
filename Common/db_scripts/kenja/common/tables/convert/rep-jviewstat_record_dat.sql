-- kanji=漢字
-- $Id: cbe1d1216fe2304e87e910b44d1866dafa2cfd14 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table JVIEWSTAT_RECORD_DAT_OLD
rename table JVIEWSTAT_RECORD_DAT TO JVIEWSTAT_RECORD_DAT_OLD

create table JVIEWSTAT_RECORD_DAT( \
    YEAR          varchar(4)  not null, \
    SEMESTER      varchar(1)  not null, \
    SCHREGNO      varchar(8)  not null, \
    CLASSCD       varchar(2)  not null, \
    SCHOOL_KIND   varchar(2)  not null, \
    CURRICULUM_CD varchar(2)  not null, \
    SUBCLASSCD    varchar(6)  not null, \
    VIEWCD        varchar(4)  not null, \
    STATUS        varchar(6), \
    SCORE         smallint, \
    REGISTERCD    varchar(10), \
    UPDATED       timestamp default current timestamp  \
    ) in usr1dms index in idx1dms

insert into JVIEWSTAT_RECORD_DAT \
    select \
        YEAR, \
        SEMESTER, \
        SCHREGNO, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        VIEWCD, \
        STATUS, \
        cast(null as smallint) as SCORE, \
        REGISTERCD, \
        UPDATED \
     from \
         JVIEWSTAT_RECORD_DAT_OLD

alter table JVIEWSTAT_RECORD_DAT  \
add constraint pk_jviewstat_rec  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)
