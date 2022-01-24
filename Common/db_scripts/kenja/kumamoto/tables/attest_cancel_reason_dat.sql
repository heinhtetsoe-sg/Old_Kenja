-- kanji=漢字
-- $Id: attest_cancel_reason_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_REASON_DAT

CREATE TABLE ATTEST_CANCEL_REASON_DAT  \
(   CANCEL_YEAR      VARCHAR(4) NOT NULL, \
    CANCEL_SEQ       SMALLINT   NOT NULL, \
    CANCEL_STAFFCD   VARCHAR(8) NOT NULL, \
    CANCEL_DATE      DATE       NOT NULL, \
    REASON           VARCHAR(122), \
    REGISTERCD       VARCHAR(8)  , \
    UPDATED          TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE ATTEST_CANCEL_REASON_DAT \
ADD CONSTRAINT PK_ATC_REASON_DAT \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ)
