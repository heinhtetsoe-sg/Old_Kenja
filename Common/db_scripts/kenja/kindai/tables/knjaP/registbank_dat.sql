-- kanji=漢字
-- $Id: registbank_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--登録銀行データ
DROP TABLE REGISTBANK_DAT

CREATE TABLE REGISTBANK_DAT \
( \
        "SCHREGNO"       VARCHAR(8)      NOT NULL, \
        "BANKCD"         VARCHAR(4),  \
        "BRANCHCD"       VARCHAR(3),  \
        "DEPOSIT_ITEM"   VARCHAR(1),  \
        "ACCOUNTNO"      VARCHAR(7),  \
        "ACCOUNTNAME"    VARCHAR(120), \
        "RELATIONSHIP"   VARCHAR(2),  \
        "REGISTERCD"     VARCHAR(8),  \
        "UPDATED"        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE REGISTBANK_DAT \
ADD CONSTRAINT PK_REGISTBANK_DAT \
PRIMARY KEY \
(SCHREGNO)
