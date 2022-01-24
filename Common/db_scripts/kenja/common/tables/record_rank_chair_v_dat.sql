-- kanji=漢字
-- $Id: b1cad7e20b057069755d903d8eaa6044844ed5c4 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table RECORD_RANK_CHAIR_V_DAT

create table RECORD_RANK_CHAIR_V_DAT ( \
    YEAR                    VARCHAR(4) NOT NULL, \
    SEMESTER                VARCHAR(1) NOT NULL, \
    TESTKINDCD              VARCHAR(2) NOT NULL, \
    TESTITEMCD              VARCHAR(2) NOT NULL, \
    CLASSCD                 VARCHAR(2) NOT NULL, \
    SCHOOL_KIND             VARCHAR(2) NOT NULL, \
    CURRICULUM_CD           VARCHAR(2) NOT NULL, \
    SUBCLASSCD              VARCHAR(6) NOT NULL, \
    CHAIRCD                 VARCHAR(7) NOT NULL, \
    SCHREGNO                VARCHAR(8) NOT NULL, \
    SCORE                   SMALLINT, \
    AVG                     DECIMAL (8,5), \
    GRADE_RANK              SMALLINT, \
    GRADE_AVG_RANK          SMALLINT, \
    GRADE_DEVIATION         DECIMAL (4,1), \
    GRADE_DEVIATION_RANK    SMALLINT, \
    CLASS_RANK              SMALLINT, \
    CLASS_AVG_RANK          SMALLINT, \
    CLASS_DEVIATION         DECIMAL (4,1), \
    CLASS_DEVIATION_RANK    SMALLINT, \
    COURSE_RANK             SMALLINT, \
    COURSE_AVG_RANK         SMALLINT, \
    COURSE_DEVIATION        DECIMAL (4,1), \
    COURSE_DEVIATION_RANK   SMALLINT, \
    MAJOR_RANK              SMALLINT, \
    MAJOR_AVG_RANK          SMALLINT, \
    MAJOR_DEVIATION         DECIMAL(4,1), \
    MAJOR_DEVIATION_RANK    SMALLINT, \
    CHAIRDATE               DATE, \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table RECORD_RANK_CHAIR_V_DAT add constraint pk_rec_r_chr_v_dt \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD, SCHREGNO)
