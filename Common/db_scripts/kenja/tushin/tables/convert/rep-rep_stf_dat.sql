-- kanji=漢字
-- $Id: rep-rep_stf_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

DROP TABLE REP_STF_DAT_OLD
RENAME TABLE REP_STF_DAT TO REP_STF_DAT_OLD

CREATE TABLE REP_STF_DAT \
    ( YEAR           VARCHAR(4) NOT NULL, \
      CLASSCD        VARCHAR(2) NOT NULL, \
      SCHOOL_KIND    VARCHAR(2) NOT NULL, \
      CURRICULUM_CD  VARCHAR(2) NOT NULL, \
      SUBCLASSCD     VARCHAR(6) NOT NULL, \
      CHAIRCD        VARCHAR(7) NOT NULL, \
      STAFFCD        VARCHAR(8) NOT NULL, \
      REGISTERCD     VARCHAR(8), \
      UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE REP_STF_DAT ADD CONSTRAINT PK_REP_STF_DAT PRIMARY KEY (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD, STAFFCD)

INSERT INTO REP_STF_DAT \
     SELECT \
         YEAR, \
         SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, \
         'H' AS SCHOOL_KIND, \
         '2' AS CURRICULUM_CD, \
         SUBCLASSCD, \
         CHAIRCD, \
         STAFFCD, \
         REGISTERCD, \
         UPDATED \
     FROM \
        REP_STF_DAT_OLD
