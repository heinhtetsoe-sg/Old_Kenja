-- $Id: 7c867dd1e54ecf2d1f7137e2a94f40e40b886e19 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ENTEXAM_INTERNAL_DECISION_YDAT
CREATE TABLE ENTEXAM_INTERNAL_DECISION_YDAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    DECISION_CD               VARCHAR(1)    NOT NULL, \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERNAL_DECISION_YDAT ADD CONSTRAINT PK_ENT_INT_DISI_Y PRIMARY KEY (ENTEXAMYEAR, DECISION_CD)
