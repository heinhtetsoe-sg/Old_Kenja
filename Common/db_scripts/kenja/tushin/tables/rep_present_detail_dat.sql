-- kanji=漢字
-- $Id: rep_present_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

DROP TABLE REP_PRESENT_DETAIL_DAT

CREATE TABLE REP_PRESENT_DETAIL_DAT \
    (  YEAR                 VARCHAR(4) NOT NULL, \
       CLASSCD              VARCHAR(2) NOT NULL, \
       SCHOOL_KIND          VARCHAR(2) NOT NULL, \
       CURRICULUM_CD        VARCHAR(2) NOT NULL, \
       SUBCLASSCD           VARCHAR(6) NOT NULL, \
       STANDARD_SEQ         SMALLINT NOT NULL, \
       REPRESENT_SEQ        SMALLINT NOT NULL, \
       SCHREGNO             VARCHAR(8) NOT NULL, \
       RECEIPT_DATE         DATE NOT NULL, \
       SEQ                  VARCHAR(3) NOT NULL, \
       REMARK1              VARCHAR(90),  \
       REMARK2              VARCHAR(90),  \
       REMARK3              VARCHAR(90),  \
       REMARK4              VARCHAR(90),  \
       REMARK5              VARCHAR(90),  \
       REGISTERCD           VARCHAR(8), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE REP_PRESENT_DETAIL_DAT ADD CONSTRAINT PK_REP_PRESENT_DET PRIMARY KEY (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, REPRESENT_SEQ, SCHREGNO, RECEIPT_DATE, SEQ)
