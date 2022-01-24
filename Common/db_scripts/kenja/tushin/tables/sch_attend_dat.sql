-- kanji=漢字
-- $Id: sch_attend_dat.sql 69615 2019-09-11 01:57:08Z yamashiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


DROP TABLE SCH_ATTEND_DAT

CREATE TABLE SCH_ATTEND_DAT \
    (YEAR               VARCHAR(4) NOT NULL, \
     SCHREGNO           VARCHAR(8) NOT NULL, \
     EXECUTEDATE        DATE       NOT NULL, \
     CHAIRCD            VARCHAR(7) NOT NULL, \
     PERIODCD           VARCHAR(1), \
     SCHOOLING_SEQ      SMALLINT, \
     RECEIPT_DATE       DATE, \
     RECEIPT_TIME       TIME, \
     STAFFCD            VARCHAR(10), \
     TERMINAL_CD        VARCHAR(5), \
     SCHOOLINGKINDCD    VARCHAR(2), \
     REMARK             VARCHAR(60), \
     CREDIT_TIME        DECIMAL(3,1), \
     REGISTERCD         VARCHAR(10), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS
