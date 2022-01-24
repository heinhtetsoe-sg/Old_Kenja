-- kanji=漢字
-- $Id: ca543d0b5bc1921ca1acd0d322c3bbde1f2d9f45 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table JVIEWNAME_SUB_YDAT_OLD
rename table JVIEWNAME_SUB_YDAT TO JVIEWNAME_SUB_YDAT_OLD

create table JVIEWNAME_SUB_YDAT( \
    YEAR                   VARCHAR(4) NOT NULL, \
    CLASSCD                VARCHAR(2) NOT NULL, \
    SCHOOL_KIND            VARCHAR(2) NOT NULL, \
    CURRICULUM_CD          VARCHAR(2) NOT NULL, \
    SUBCLASSCD             VARCHAR(6) NOT NULL, \
    VIEWCD                 VARCHAR(4) NOT NULL, \
    REGISTERCD             VARCHAR(8), \
    UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms
    
insert into  JVIEWNAME_SUB_YDAT \
    select \
        YEAR, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        VIEWCD, \
        REGISTERCD, \
        UPDATED \
     from \
         JVIEWNAME_SUB_YDAT_OLD

ALTER TABLE JVIEWNAME_SUB_YDAT ADD CONSTRAINT PK_JVN_SUB_YDAT PRIMARY KEY (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)

