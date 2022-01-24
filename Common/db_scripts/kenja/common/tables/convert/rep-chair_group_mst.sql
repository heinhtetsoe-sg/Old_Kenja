-- kanji=漢字
-- $Id: a739f273c692bba65c816f479b6d8a6a9a59deb3 $
--
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f convert_chair_dat.sql
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop   table CHAIR_GROUP_MST_OLD

rename table CHAIR_GROUP_MST     to CHAIR_GROUP_MST_OLD

create table CHAIR_GROUP_MST(  \
        YEAR              VARCHAR(4) NOT NULL, \
        SEMESTER          VARCHAR(1) NOT NULL, \
        CHAIR_GROUP_CD    VARCHAR(6) NOT NULL, \
        CLASSCD           VARCHAR(2) NOT NULL, \
        SCHOOL_KIND       VARCHAR(2) NOT NULL, \
        CURRICULUM_CD     VARCHAR(2) NOT NULL, \
        SUBCLASSCD        VARCHAR(6) NOT NULL, \
        CHAIR_GROUP_NAME  VARCHAR(60), \
        CHAIR_GROUP_ABBV  VARCHAR(60), \
        REGISTERCD        VARCHAR(10), \
        UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms


insert into CHAIR_GROUP_MST \
  select \
        YEAR, \
        SEMESTER, \
        CHAIR_GROUP_CD, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        CHAIR_GROUP_NAME, \
        CHAIR_GROUP_NAME, \
        REGISTERCD, \
        UPDATED \
  from CHAIR_GROUP_MST_OLD

alter table CHAIR_GROUP_MST  \
add constraint PK_CHAIR_GROUP_MST \
primary key  \
(YEAR, SEMESTER, CHAIR_GROUP_CD)
