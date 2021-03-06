-- kanji=漢字
-- $Id: 59579381a06ccbd54a891f23ae9e6745e273e4d9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE GRD_TRANSFER_DAT_OLD
RENAME TABLE GRD_TRANSFER_DAT TO GRD_TRANSFER_DAT_OLD

CREATE TABLE GRD_TRANSFER_DAT \
    (SCHREGNO         VARCHAR(8)    NOT NULL, \
     TRANSFERCD       VARCHAR(2)    NOT NULL, \
     TRANSFER_SDATE   DATE          NOT NULL, \
     TRANSFER_EDATE   DATE, \
     TRANSFERREASON   VARCHAR(75), \
     TRANSFERPLACE    VARCHAR(60), \
     TRANSFERADDR     VARCHAR(75), \
     ABROAD_CLASSDAYS SMALLINT, \
     ABROAD_CREDITS   SMALLINT, \
     ABROAD_PRINT_DROP_REGD VARCHAR(1), \
     REMARK1          VARCHAR(90), \
     REMARK2          VARCHAR(90), \
     REGISTERCD       VARCHAR(10), \
     UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    )IN USR1DMS INDEX IN IDX1DMS

INSERT INTO GRD_TRANSFER_DAT \
   SELECT \
     SCHREGNO, \
     TRANSFERCD, \
     TRANSFER_SDATE, \
     TRANSFER_EDATE, \
     TRANSFERREASON, \
     TRANSFERPLACE, \
     TRANSFERADDR, \
     ABROAD_CLASSDAYS, \
     ABROAD_CREDITS, \
     CAST(NULL AS VARCHAR(1)) AS ABROAD_PRINT_DROP_REGD, \
     CAST(NULL AS VARCHAR(1)) AS REMARK1, \
     CAST(NULL AS VARCHAR(1)) AS REMARK2, \
     REGISTERCD, \
     UPDATED \
    FROM \
        GRD_TRANSFER_DAT_OLD

ALTER TABLE GRD_TRANSFER_DAT ADD CONSTRAINT PK_GRD_TRANSFER PRIMARY KEY \
    (SCHREGNO, TRANSFERCD, TRANSFER_SDATE)
