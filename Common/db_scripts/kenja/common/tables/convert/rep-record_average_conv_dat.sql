-- kanji=漢字
-- $Id: 31f0ea54c21ae8ce7b7426a5ead8c4b1c00f0b4f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table RECORD_AVERAGE_CONV_DAT_OLD

rename table RECORD_AVERAGE_CONV_DAT to RECORD_AVERAGE_CONV_DAT_OLD

create table RECORD_AVERAGE_CONV_DAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    TESTKINDCD      VARCHAR(2) NOT NULL, \
    TESTITEMCD      VARCHAR(2) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
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
    REGISTERCD      VARCHAR(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

insert into RECORD_AVERAGE_CONV_DAT \
 select \
    YEAR,          \
    SEMESTER,      \
    TESTKINDCD,    \
    TESTITEMCD,    \
    substr(SUBCLASSCD, 1, 2), \
    'H' as SCHOOL_KIND,    \
    '2' as CURRICULUM_CD,  \
    SUBCLASSCD,    \
    AVG_DIV,       \
    GRADE,         \
    HR_CLASS,      \
    COURSECD,      \
    MAJORCD,       \
    COURSECODE,    \
    SCORE,         \
    HIGHSCORE,     \
    LOWSCORE,      \
    COUNT,         \
    AVG,           \
    STDDEV,        \
    REGISTERCD,    \
    UPDATED        \
 from \
  RECORD_AVERAGE_CONV_DAT_OLD

alter table RECORD_AVERAGE_CONV_DAT add constraint pk_record_avg_cvd \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
