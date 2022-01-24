-- kanji=漢字
-- $Id: acb8a7fadf5b4f5ab13007b3e95f265fccfa075a $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table RECORD_AVERAGE_V_DAT

create table RECORD_AVERAGE_V_DAT ( \
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

alter table RECORD_AVERAGE_V_DAT add constraint pk_rec_avg_v_dat \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
