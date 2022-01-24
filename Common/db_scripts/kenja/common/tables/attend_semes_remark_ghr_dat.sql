-- kanji=漢字
-- $Id: 95288636719013e53c131d79b497f558408a7490 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEND_SEMES_REMARK_GHR_DAT

CREATE TABLE ATTEND_SEMES_REMARK_GHR_DAT \
        (YEAR               VARCHAR(4)      NOT NULL, \
         MONTH              VARCHAR(2)      NOT NULL, \
         SEMESTER           VARCHAR(1)      NOT NULL, \
         GHR_CD             VARCHAR(2)      NOT NULL, \
         REMARK1            VARCHAR(150), \
         REMARK2            VARCHAR(150), \
         REMARK3            VARCHAR(150), \
         REGISTERCD         VARCHAR(10), \
         UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
        ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SEMES_REMARK_GHR_DAT ADD CONSTRAINT PK_ATTSEMS_R_GHR PRIMARY KEY \
        (YEAR, MONTH, SEMESTER, GHR_CD)
