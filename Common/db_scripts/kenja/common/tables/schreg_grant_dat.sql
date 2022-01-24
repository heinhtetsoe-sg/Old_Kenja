-- kanji=漢字
-- $Id: e68f6e6aafebefcec7e92fd8a1a577495c5147da $

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
        "REGISTERCD"    VARCHAR(10), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_GRANT_DAT \
ADD CONSTRAINT PK_SCREG_GRANT_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,GRANTCD)

