-- $Id: 24ef85dddbad65e4a2cd1fe80e6461bd5a5a2584 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ENTEXAM_INTERNAL_DECISION_MST
CREATE TABLE ENTEXAM_INTERNAL_DECISION_MST( \
    DECISION_CD               VARCHAR(1)    NOT NULL, \
    DECISION_NAME             VARCHAR(120), \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERNAL_DECISION_MST ADD CONSTRAINT PK_ENT_INT_DISI_M PRIMARY KEY (DECISION_CD)
