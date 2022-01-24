-- kanji=漢字
-- $Id: 523eb83d0a8b43d0d3a8ae9977e7d2008cd46f1a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table REP_PRESENT_SEMES_DAT

create table REP_PRESENT_SEMES_DAT \
    (YEAR                 varchar(4) not null, \
     SEMESTER           varchar(1) not null, \
     CLASSCD              varchar(2) not null, \
     SCHOOL_KIND          varchar(2) not null, \
     CURRICULUM_CD        varchar(2) not null, \
     SUBCLASSCD           varchar(6) not null, \
     SCHREGNO             varchar(8) not null, \
     CHAIRCD              varchar(7), \
     REPORT_CNT           smallint, \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table REP_PRESENT_SEMES_DAT add constraint PK_REP_PRESENT_SEM primary key (YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)

