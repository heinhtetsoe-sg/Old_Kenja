-- kanji=漢字
-- $Id: 7a345556f690140a513c2a6b8f5d54949e0bc8ce $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_REPLACE_DAT

create table SUBCLASS_REPLACE_DAT ( \
    REPLACECD                   VARCHAR(1) NOT NULL, \
    YEAR                        VARCHAR(4) NOT NULL, \
    ANNUAL                      VARCHAR(2) NOT NULL, \
    ATTEND_CLASSCD              VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND          VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD        VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD           VARCHAR(6) NOT NULL, \
    GRADING_CLASSCD             VARCHAR(2) NOT NULL, \
    GRADING_SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    GRADING_CURRICULUM_CD       VARCHAR(2) NOT NULL, \
    GRADING_SUBCLASSCD          VARCHAR(6) NOT NULL, \
    REGISTERCD                  varchar(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table SUBCLASS_REPLACE_DAT add constraint PK_SUBCLASS_RP_DAT \
        primary key (REPLACECD, YEAR, ANNUAL, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD, GRADING_CLASSCD, GRADING_SCHOOL_KIND, GRADING_CURRICULUM_CD, GRADING_SUBCLASSCD)
