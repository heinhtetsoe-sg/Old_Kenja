-- kanji=漢字
-- $Id: attest_cancel_schreg_studyrec_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT

CREATE TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT  \
(   CANCEL_YEAR      VARCHAR(4) NOT NULL , \
    CANCEL_SEQ       SMALLINT   NOT NULL , \
    CANCEL_STAFFCD   VARCHAR(8) NOT NULL , \
    SCHOOLCD         VARCHAR(1) NOT NULL , \
    YEAR             VARCHAR(4) NOT NULL , \
    SCHREGNO         VARCHAR(8) NOT NULL , \
    ANNUAL           VARCHAR(2) NOT NULL , \
    CLASSCD          VARCHAR(2) NOT NULL , \
    SUBCLASSCD       VARCHAR(6) NOT NULL , \
    CLASSNAME        VARCHAR(30)  , \
    CLASSABBV        VARCHAR(15)  , \
    CLASSNAME_ENG    VARCHAR(40)  , \
    CLASSABBV_ENG    VARCHAR(30)  , \
    SUBCLASSES       SMALLINT , \
    SUBCLASSNAME     VARCHAR(90) , \
    SUBCLASSABBV     VARCHAR(90) , \
    SUBCLASSNAME_ENG VARCHAR(40) , \
    SUBCLASSABBV_ENG VARCHAR(20) , \
    VALUATION        SMALLINT , \
    GET_CREDIT       SMALLINT , \
    ADD_CREDIT       SMALLINT , \
    COMP_CREDIT      SMALLINT , \
    PRINT_FLG        VARCHAR(1)  , \
    REGISTERCD       VARCHAR(8)  , \
    UPDATED          TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT \
ADD CONSTRAINT PK_ATC_SCHREGSTUDY \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, SCHOOLCD, YEAR, SCHREGNO, ANNUAL, CLASSCD, SUBCLASSCD)
