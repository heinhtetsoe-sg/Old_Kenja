-- kanji=漢字
-- $Id: a4ed098da53af35ad481c8dcd798c27f138076b2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table RECORD_CHKFIN_DAT

create table RECORD_CHKFIN_DAT \
(  \
    YEAR            VARCHAR (4) NOT NULL, \
    SEMESTER        VARCHAR (1) NOT NULL, \
    CLASSCD         VARCHAR (2) NOT NULL, \
    SCHOOL_KIND     VARCHAR (2) NOT NULL, \
    CURRICULUM_CD   VARCHAR (2) NOT NULL, \
    SUBCLASSCD      VARCHAR (6) NOT NULL, \
    CHAIRCD         VARCHAR (7) NOT NULL, \
    TESTKINDCD      VARCHAR (2) NOT NULL, \
    TESTITEMCD      VARCHAR (2) NOT NULL, \
    RECORD_DIV      VARCHAR (1) NOT NULL, \
    EXECUTEDATE     DATE, \
    EXECUTED        VARCHAR (1), \
    REGISTERCD      VARCHAR (8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table RECORD_CHKFIN_DAT add constraint PK_RECORD_CF_DAT \
primary key (YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD, TESTKINDCD, TESTITEMCD, RECORD_DIV)
