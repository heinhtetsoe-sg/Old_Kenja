-- kanji=漢字
-- $Id: 3c6c984aa9597096c1f97b63d5291d4935684c72 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table JVIEWSTAT_RECORD_TARGETGRADE_DAT

create table JVIEWSTAT_RECORD_TARGETGRADE_DAT(  \
    YEAR                   varchar(4)  not null, \
    SEMESTER               varchar(1)  not null, \
    SCHREGNO               varchar(8)  not null, \
    CLASSCD                varchar(2)  not null, \
    SCHOOL_KIND            varchar(2)  not null, \
    CURRICULUM_CD          varchar(2)  not null, \
    SUBCLASSCD             varchar(6)  not null, \
    TARGET_VIEWCD          varchar(4)  not null, \
    TARGET_GRADE           varchar(2)  , \
    TARGET_CLASSCD         varchar(2)  , \
    TARGET_SCHOOL_KIND     varchar(2)  , \
    TARGET_CURRICULUM_CD   varchar(2)  , \
    TARGET_SUBCLASSCD      varchar(6)  , \
    STATUS                 varchar(3)  , \
    REGISTERCD             varchar(10) , \
    UPDATED                timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table JVIEWSTAT_RECORD_TARGETGRADE_DAT  \
add constraint pk_jviewstat_rec  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TARGET_VIEWCD)
