-- kanji=漢字
-- $Id: 0b313f44fdd1aefc76c68d045e4c9744b16f5ff6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CHAIR_CORRES_SEMES_DAT_BK
create table CHAIR_CORRES_SEMES_DAT_BK LIKE CHAIR_CORRES_SEMES_DAT
insert into CHAIR_CORRES_SEMES_DAT_BK select * from CHAIR_CORRES_SEMES_DAT

drop table CHAIR_CORRES_SEMES_DAT

create table CHAIR_CORRES_SEMES_DAT ( \
    YEAR                varchar(4) not null, \
    SEMESTER            varchar(1) not null, \
    CHAIRCD             varchar(7) not null, \
    CLASSCD             varchar(2) not null, \
    SCHOOL_KIND         varchar(2) not null, \
    CURRICULUM_CD       varchar(2) not null, \
    SUBCLASSCD          varchar(6) not null, \
    REPO_MAX_CNT        smallint, \
    REPO_LIMIT_CNT      smallint, \
    SCHOOLING_MAX_CNT   smallint, \
    SCHOOLING_LIMIT_CNT smallint, \
    USE_MEDIA1          varchar(1), \
    USE_MEDIA2          varchar(1), \
    USE_MEDIA3          varchar(1), \
    USE_MEDIA4          varchar(1), \
    USE_MEDIA5          varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHAIR_CORRES_SEMES_DAT add constraint PK_CHR_CORS_SEM_D primary key (YEAR, SEMESTER, CHAIRCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)

insert into CHAIR_CORRES_SEMES_DAT \
select \
    YEAR, \
    SEMESTER, \
    CHAIRCD, \
    CLASSCD, \
    SCHOOL_KIND, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    REPO_MAX_CNT, \
    REPO_LIMIT_CNT, \
    SCHOOLING_MAX_CNT, \
    SCHOOLING_LIMIT_CNT, \
    cast(null as varchar(1)) AS USE_MEDIA1, \
    cast(null as varchar(1)) AS USE_MEDIA2, \
    cast(null as varchar(1)) AS USE_MEDIA3, \
    cast(null as varchar(1)) AS USE_MEDIA4, \
    cast(null as varchar(1)) AS USE_MEDIA5, \
    REGISTERCD, \
    UPDATED \
from CHAIR_CORRES_SEMES_DAT_BK
