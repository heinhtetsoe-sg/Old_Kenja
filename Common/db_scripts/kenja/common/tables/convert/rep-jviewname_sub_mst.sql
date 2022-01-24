-- kanji=漢字
-- $Id: c2d8be687cef3c48877a3033ed6ab49613ed7962 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table JVIEWNAME_SUB_MST_OLD
rename table JVIEWNAME_SUB_MST TO JVIEWNAME_SUB_MST_OLD

create table JVIEWNAME_SUB_MST( \
    CLASSCD                VARCHAR(2) NOT NULL, \
    SCHOOL_KIND            VARCHAR(2) NOT NULL, \
    CURRICULUM_CD          VARCHAR(2) NOT NULL, \
    SUBCLASSCD             VARCHAR(6) NOT NULL, \
    VIEWCD                 VARCHAR(4) NOT NULL, \
    VIEWNAME               VARCHAR(75), \
    VIEWABBV               VARCHAR(60), \
    SHOWORDER              SMALLINT, \
    WEIGHT                 SMALLINT, \
    REGISTERCD             VARCHAR(8), \
    UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms
    
insert into  JVIEWNAME_SUB_MST \
    select \
         CLASSCD, \
         SCHOOL_KIND, \
         CURRICULUM_CD, \
         SUBCLASSCD, \
         VIEWCD, \
         VIEWNAME, \
         VIEWABBV, \
         SHOWORDER, \
         CAST(NULL AS SMALLINT) AS WEIGHT, \
         REGISTERCD, \
         UPDATED \
     from \
         JVIEWNAME_SUB_MST_OLD

alter table JVIEWNAME_SUB_MST \
    add constraint PK_JVN_SUB_MST \
    primary key \
    (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)

