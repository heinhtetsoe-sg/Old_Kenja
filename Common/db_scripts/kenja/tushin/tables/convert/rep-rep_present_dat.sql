-- kanji=漢字
-- $Id: rep-rep_present_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

DROP TABLE REP_PRESENT_DAT_OLD
RENAME TABLE REP_PRESENT_DAT TO REP_PRESENT_DAT_OLD

CREATE TABLE REP_PRESENT_DAT \
    (  YEAR                 VARCHAR(4) NOT NULL, \
       CLASSCD              VARCHAR(2) NOT NULL, \
       SCHOOL_KIND          VARCHAR(2) NOT NULL, \
       CURRICULUM_CD        VARCHAR(2) NOT NULL, \
       SUBCLASSCD           VARCHAR(6) NOT NULL, \
       STANDARD_SEQ         SMALLINT NOT NULL, \
       REPRESENT_SEQ        SMALLINT NOT NULL, \
       SCHREGNO             VARCHAR(8) NOT NULL, \
       RECEIPT_DATE         DATE NOT NULL, \
       RECEIPT_INPUT_DATE   DATE, \
       RECEIPT_TIME         TIME, \
       CHAIRCD              VARCHAR(7), \
       STAFFCD              VARCHAR(8), \
       TERMINAL_CD          VARCHAR(5), \
       GRAD_VALUE           VARCHAR(2), \
       GRAD_DATE            DATE, \
       GRAD_INPUT_DATE      DATE, \
       GRAD_TIME            TIME, \
       REPRESENT_PRINT      VARCHAR(1), \
       REGISTERCD           VARCHAR(8), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE REP_PRESENT_DAT ADD CONSTRAINT PK_REP_PRESENT_DAT PRIMARY KEY (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, REPRESENT_SEQ, SCHREGNO, RECEIPT_DATE)

INSERT INTO REP_PRESENT_DAT \
     SELECT \
         YEAR, \
         CLASSCD, \
         SCHOOL_KIND, \
         CURRICULUM_CD, \
         SUBCLASSCD, \
         STANDARD_SEQ, \
         REPRESENT_SEQ, \
         SCHREGNO, \
         RECEIPT_DATE, \
         CAST(NULL AS DATE), \
         RECEIPT_TIME, \
         CHAIRCD, \
         STAFFCD, \
         TERMINAL_CD, \
         GRAD_VALUE, \
         GRAD_DATE, \
         CAST(NULL AS DATE), \
         GRAD_TIME, \
         REPRESENT_PRINT, \
         REGISTERCD, \
         UPDATED \
     FROM \
        REP_PRESENT_DAT_OLD
