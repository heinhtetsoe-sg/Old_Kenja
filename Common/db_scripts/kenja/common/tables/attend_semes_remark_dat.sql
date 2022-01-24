-- kanji=漢字
-- $Id: 5d2d96098091c53a3c6aad1a9d70f65069273d7f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEND_SEMES_REMARK_DAT

CREATE TABLE ATTEND_SEMES_REMARK_DAT \
        (COPYCD             VARCHAR(1)      NOT NULL, \
         YEAR               VARCHAR(4)      NOT NULL, \
         MONTH              VARCHAR(2)      NOT NULL, \
         SEMESTER           VARCHAR(1)      NOT NULL, \
         SCHREGNO           VARCHAR(8)      NOT NULL, \
         REMARK1            VARCHAR(150), \
         REMARK2            VARCHAR(150), \
         REMARK3            VARCHAR(150), \
         REGISTERCD         VARCHAR(8), \
         UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
        ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SEMES_REMARK_DAT ADD CONSTRAINT PK_ATTSEMS_DAT PRIMARY KEY \
        (COPYCD, YEAR, MONTH, SEMESTER, SCHREGNO)


