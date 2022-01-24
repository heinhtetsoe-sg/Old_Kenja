-- kanji=漢字
-- $Id: 869776ecfd02dc64a999e4e24f773cd322fd87fe $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE JOBTYPE_L_MST
CREATE TABLE JOBTYPE_L_MST( \
    JOBTYPE_LCD        VARCHAR(2)    NOT NULL, \
    JOBTYPE_LNAME      VARCHAR(150), \
    JOBTYPE_LNAME_KANA VARCHAR(300), \
    REGISTERCD         VARCHAR(10), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JOBTYPE_L_MST ADD CONSTRAINT PK_JOBTYPE_L_MST PRIMARY KEY (JOBTYPE_LCD)