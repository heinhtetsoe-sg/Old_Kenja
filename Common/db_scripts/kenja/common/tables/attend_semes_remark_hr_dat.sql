-- kanji=漢字
-- $Id: a58411e1a2e1e248da3ce4718f90fdbe03e6c317 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEND_SEMES_REMARK_HR_DAT

CREATE TABLE ATTEND_SEMES_REMARK_HR_DAT \
        (YEAR               VARCHAR(4)      NOT NULL, \
         MONTH              VARCHAR(2)      NOT NULL, \
         SEMESTER           VARCHAR(1)      NOT NULL, \
         GRADE              VARCHAR(2)      NOT NULL, \
         HR_CLASS           VARCHAR(3)      NOT NULL, \
         REMARK1            VARCHAR(150), \
         REMARK2            VARCHAR(150), \
         REMARK3            VARCHAR(150), \
         REGISTERCD         VARCHAR(10), \
         UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
        ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SEMES_REMARK_HR_DAT ADD CONSTRAINT PK_ATTSEMS_R_HR PRIMARY KEY \
        (YEAR, MONTH, SEMESTER, GRADE, HR_CLASS)
