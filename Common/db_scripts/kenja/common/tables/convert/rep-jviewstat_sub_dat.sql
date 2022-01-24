-- kanji=漢字
-- $Id: 833f5801e76d453da6a367a2f6364a10a870ecfc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table JVIEWSTAT_SUB_DAT_OLD
rename table JVIEWSTAT_SUB_DAT TO JVIEWSTAT_SUB_DAT_OLD

create table JVIEWSTAT_SUB_DAT( \
        YEAR          VARCHAR(4)  NOT NULL, \
        SEMESTER      VARCHAR(1)  NOT NULL, \
        SCHREGNO      VARCHAR(8)  NOT NULL, \
        CLASSCD       VARCHAR(2)  NOT NULL, \
        SCHOOL_KIND   VARCHAR(2)  NOT NULL, \
        CURRICULUM_CD VARCHAR(2)  NOT NULL, \
        SUBCLASSCD    VARCHAR(6)  NOT NULL, \
        VIEWCD        VARCHAR(4)  NOT NULL, \
        STATUS        VARCHAR(1),  \
        REGISTERCD    VARCHAR(8),  \
        UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms

    
insert into  JVIEWSTAT_SUB_DAT \
    select \
        YEAR, \
        SEMESTER, \
        SCHREGNO, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        VIEWCD, \
        STATUS, \
        REGISTERCD, \
        UPDATED \
     from \
         JVIEWSTAT_SUB_DAT_OLD

alter table JVIEWSTAT_SUB_DAT  \
add constraint PK_JVS_SUB_DAT  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)

