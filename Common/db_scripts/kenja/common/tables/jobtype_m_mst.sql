-- kanji=漢字
-- $Id: 2acb951cf8cf743305b19b2d4a54e4d6b8022feb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE JOBTYPE_M_MST
CREATE TABLE JOBTYPE_M_MST( \
    JOBTYPE_LCD        VARCHAR(2)    NOT NULL, \
    JOBTYPE_MCD        VARCHAR(2)    NOT NULL, \
    JOBTYPE_MNAME      VARCHAR(150), \
    JOBTYPE_MNAME_KANA VARCHAR(300), \
    REGISTERCD         VARCHAR(10), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JOBTYPE_M_MST ADD CONSTRAINT PK_JOBTYPE_M_MST PRIMARY KEY (JOBTYPE_LCD,JOBTYPE_MCD)