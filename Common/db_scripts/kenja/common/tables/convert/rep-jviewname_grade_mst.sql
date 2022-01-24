-- kanji=漢字
-- $Id: b1a83b9013172872cf31d0db16868a6baeff979d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table JVIEWNAME_GRADE_MST_OLD
rename table JVIEWNAME_GRADE_MST TO JVIEWNAME_GRADE_MST_OLD

create table JVIEWNAME_GRADE_MST( \
        GRADE                  VARCHAR(2) NOT NULL, \
        CLASSCD                VARCHAR(2) NOT NULL, \
        SCHOOL_KIND            VARCHAR(2) NOT NULL, \
        CURRICULUM_CD          VARCHAR(2) NOT NULL, \
        SUBCLASSCD             VARCHAR(6) NOT NULL, \
        VIEWCD                 VARCHAR(4) NOT NULL, \
        VIEWNAME               VARCHAR(300), \
        VIEWABBV               VARCHAR(60), \
        SHOWORDER              SMALLINT, \
        STUDYREC_CLASSCD       VARCHAR(2), \
        STUDYREC_SCHOOL_KIND   VARCHAR(2), \
        STUDYREC_CURRICULUM_CD VARCHAR(2), \
        STUDYREC_SUBCLASSCD    VARCHAR(6), \
        STUDYREC_VIEWCD        VARCHAR(4), \
        REGISTERCD             VARCHAR(8), \
     UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms
    
insert into  JVIEWNAME_GRADE_MST \
    select \
         GRADE, \
         LEFT(SUBCLASSCD, 2) AS CLASSCD, \
         'H' AS SCHOOL_KIND, \
         '2' AS CURRICULUM_CD, \
         SUBCLASSCD, \
         VIEWCD, \
         VIEWNAME, \
         VIEWABBV, \
         SHOWORDER, \
         LEFT(STUDYREC_SUBCLASSCD, 2) AS STUDYREC_CLASSCD, \
         'H' AS STUDYREC_SCHOOL_KIND, \
         '2' AS STUDYREC_CURRICULUM_CD, \
         STUDYREC_SUBCLASSCD, \
         STUDYREC_VIEWCD, \
         REGISTERCD, \
         UPDATED \
     from \
         JVIEWNAME_GRADE_MST_OLD

alter table JVIEWNAME_GRADE_MST \
    add constraint PK_JVN_GRADE_MST \
    primary key \
    (GRADE,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,VIEWCD)

