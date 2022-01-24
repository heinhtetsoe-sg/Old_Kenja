-- kanji=漢字
-- $Id: attest_cancel_behavior_dat.sql 59758 2018-04-16 14:21:49Z yamashiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_BEHAVIOR_DAT

CREATE TABLE ATTEST_CANCEL_BEHAVIOR_DAT  \
(   CANCEL_YEAR      VARCHAR(4)  NOT NULL,  \
    CANCEL_SEQ       SMALLINT    NOT NULL,  \
    CANCEL_STAFFCD   VARCHAR(10) NOT NULL,  \
    YEAR             VARCHAR(4)  NOT NULL,  \
    SCHREGNO         VARCHAR(8)  NOT NULL,  \
    DIV              VARCHAR(1)  NOT NULL,  \
    CODE             VARCHAR(2)  NOT NULL,  \
    ANNUAL           VARCHAR(2)  NOT NULL,  \
    RECORD           VARCHAR(1),  \
    REGISTERCD       VARCHAR(10),  \
    UPDATED          TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEST_CANCEL_BEHAVIOR_DAT  \
ADD CONSTRAINT PK_ATC_BEHAVIOR  \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, YEAR, SCHREGNO, DIV, CODE)
