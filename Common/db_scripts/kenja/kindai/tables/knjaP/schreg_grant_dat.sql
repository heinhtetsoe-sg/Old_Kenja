-- kanji=漢字
-- $Id: schreg_grant_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--SCHREG_GRANT_DAT	生徒交付データ

DROP TABLE SCHREG_GRANT_DAT

CREATE TABLE SCHREG_GRANT_DAT \
( \
        "YEAR"          VARCHAR(4)  NOT NULL, \
        "SCHREGNO"      VARCHAR(8)  NOT NULL, \
        "GRANTCD"       VARCHAR(2)  NOT NULL, \
        "GRANTSDATE"    DATE, \
        "GRANTEDATE"    DATE, \
        "GRANT_MONEY"   INTEGER, \
        "REMARK"        VARCHAR(75), \
        "REGISTERCD"    VARCHAR(8), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_GRANT_DAT \
ADD CONSTRAINT PK_SCREG_GRANT_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,GRANTCD)

