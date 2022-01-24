-- kanji=漢字
-- $Id: eea147bcb1223c7b077c5db515a2eae37c6172e1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table RECORD_SCORE_DAT

create table RECORD_SCORE_DAT \
      (YEAR           VARCHAR(4) NOT NULL, \
       SEMESTER       VARCHAR(1) NOT NULL, \
       TESTKINDCD     VARCHAR(2) NOT NULL, \
       TESTITEMCD     VARCHAR(2) NOT NULL, \
       SCORE_DIV      VARCHAR(2) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       CHAIRCD        VARCHAR(7), \
       SCORE          SMALLINT, \
       VALUE          SMALLINT, \
       VALUE_DI       VARCHAR(2), \
       GET_CREDIT     SMALLINT, \
       ADD_CREDIT     SMALLINT, \
       COMP_TAKESEMES VARCHAR(1), \
       COMP_CREDIT    SMALLINT, \
       REGISTERCD     VARCHAR(8), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table RECORD_SCORE_DAT add constraint pk_record_score_dt \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)
