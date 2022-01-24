-- kanji=漢字
-- $Id: e1933fb4d3f51c0ab961820cc07c8c3e275daed6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE RECORD_AVERAGE_CHAIR_DAT_OLD
RENAME TABLE RECORD_AVERAGE_CHAIR_DAT TO RECORD_AVERAGE_CHAIR_DAT_OLD
CREATE TABLE RECORD_AVERAGE_CHAIR_DAT( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    TESTKINDCD      VARCHAR(2) NOT NULL, \
    TESTITEMCD      VARCHAR(2) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    CHAIRCD         VARCHAR(7) NOT NULL, \
    AVG_DIV         VARCHAR(1) NOT NULL, \
    GRADE           VARCHAR(2) NOT NULL, \
    HR_CLASS        VARCHAR(3) NOT NULL, \
    COURSECD        VARCHAR(1) NOT NULL, \
    MAJORCD         VARCHAR(3) NOT NULL, \
    COURSECODE      VARCHAR(4) NOT NULL, \
    SCORE           INTEGER, \
    HIGHSCORE       INTEGER, \
    LOWSCORE        INTEGER, \
    COUNT           SMALLINT, \
    AVG             DECIMAL (9,5), \
    STDDEV          DECIMAL (5,1), \
    CHAIRDATE       DATE, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

insert into RECORD_AVERAGE_CHAIR_DAT \
SELECT \
    YEAR, \
    SEMESTER, \
    TESTKINDCD, \
    TESTITEMCD, \
    LEFT(SUBCLASSCD, 2) AS CLASSCD, \
    'H' AS SCHOOL_KIND, \
    '2' AS CURRICULUM_CD, \
    SUBCLASSCD, \
    CHAIRCD, \
    AVG_DIV, \
    GRADE, \
    HR_CLASS, \
    COURSECD, \
    MAJORCD, \
    COURSECODE, \
    SCORE, \
    HIGHSCORE, \
    LOWSCORE, \
    COUNT, \
    AVG, \
    STDDEV, \
    CHAIRDATE, \
    REGISTERCD, \
    UPDATED \
FROM \
RECORD_AVERAGE_CHAIR_DAT_OLD

alter table RECORD_AVERAGE_CHAIR_DAT add constraint pk_rec_avg_chr_dat \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
