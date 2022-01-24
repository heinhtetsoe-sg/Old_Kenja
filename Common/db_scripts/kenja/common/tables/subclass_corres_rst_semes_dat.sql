-- kanji=漢字
-- $Id: 5b6ce4d4f926b077a9a2a9532508d2faa3e7ccb1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table SUBCLASS_CORRES_RST_SEMES_DAT

create table SUBCLASS_CORRES_RST_SEMES_DAT \
    (YEAR                 varchar(4) not null, \
     SEMESTER             varchar(1) not null, \
     CLASSCD              varchar(2) not null, \
     SCHOOL_KIND          varchar(2) not null, \
     CURRICULUM_CD        varchar(2) not null, \
     SUBCLASSCD           varchar(6) not null, \
     RST_DIV              varchar(2) not null, \
     SEQ                  smallint not null, \
     SCHREGNO             varchar(8) not null, \
     VAL_NUMERIC          smallint, \
     VAL_CHAR             varchar(30), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SUBCLASS_CORRES_RST_SEMES_DAT add constraint PK_SUB_CORRES_RST primary key (YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, RST_DIV, SEQ, SCHREGNO)

