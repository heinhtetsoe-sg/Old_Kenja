-- kanji=漢字
-- $Id: b8e2f8df371146f70748e650f368c02e0bf13e4d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table REC_SUBCLASS_GROUP_DAT

create table REC_SUBCLASS_GROUP_DAT( \
     YEAR               VARCHAR(4) NOT NULL, \
     GROUP_DIV          VARCHAR(2) NOT NULL, \
     GRADE              VARCHAR(2) NOT NULL, \
     COURSECD           VARCHAR(1) NOT NULL, \
     MAJORCD            VARCHAR(3) NOT NULL, \
     COURSECODE         VARCHAR(4) NOT NULL, \
     CLASSCD            VARCHAR(2) NOT NULL, \
     SCHOOL_KIND        VARCHAR(2) NOT NULL, \
     CURRICULUM_CD      VARCHAR(2) NOT NULL, \
     SUBCLASSCD         VARCHAR(6) NOT NULL, \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table REC_SUBCLASS_GROUP_DAT add constraint PK_REC_SUBCLASS_G primary key (YEAR, GROUP_DIV, GRADE, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)


