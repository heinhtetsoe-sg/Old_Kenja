-- kanji=漢字
-- $Id: 70291100f5c1009d3ba928e3bdb39b94523b5300 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table JVIEWNAME_GRADE_YDAT_OLD
rename table JVIEWNAME_GRADE_YDAT TO JVIEWNAME_GRADE_YDAT_OLD

create table JVIEWNAME_GRADE_YDAT( \
    YEAR                  VARCHAR(4)    NOT NULL, \
    GRADE                 VARCHAR(2)    NOT NULL, \
    CLASSCD               VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND           VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD         VARCHAR(2)    NOT NULL, \
    SUBCLASSCD            VARCHAR(6)    NOT NULL, \
    VIEWCD                VARCHAR(4)    NOT NULL, \
    REGISTERCD            VARCHAR(8), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

    
insert into  JVIEWNAME_GRADE_YDAT \
    select \
        YEAR, \
        GRADE, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        VIEWCD, \
        REGISTERCD, \
        UPDATED \
     from \
         JVIEWNAME_GRADE_YDAT_OLD

ALTER TABLE JVIEWNAME_GRADE_YDAT ADD CONSTRAINT PK_JVN_GRADE_YDAT PRIMARY KEY (YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)

