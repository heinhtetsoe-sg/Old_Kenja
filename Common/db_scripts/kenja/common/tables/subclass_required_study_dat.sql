-- kanji=漢字
-- $Id: 8c9b4f7db18ce157fc0d109fd921842e4d70fb8d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_REQUIRED_STUDY_DAT

create table SUBCLASS_REQUIRED_STUDY_DAT( \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     CURRICULUM_CD  VARCHAR(2) NOT NULL, \
     SUBCLASSCD     VARCHAR(6) NOT NULL, \
     COURSECD       VARCHAR(1) NOT NULL, \
     MAJORCD        VARCHAR(3) NOT NULL, \
     SEQ            VARCHAR(2) NOT NULL, \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table SUBCLASS_REQUIRED_STUDY_DAT add constraint PK_SUBREQUIRE primary key \
(CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, COURSECD, MAJORCD, SEQ)
