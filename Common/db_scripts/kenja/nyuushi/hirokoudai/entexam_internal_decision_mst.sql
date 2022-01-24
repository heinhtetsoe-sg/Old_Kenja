-- $Id: 24ef85dddbad65e4a2cd1fe80e6461bd5a5a2584 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ENTEXAM_INTERNAL_DECISION_MST
CREATE TABLE ENTEXAM_INTERNAL_DECISION_MST( \
    DECISION_CD               VARCHAR(1)    NOT NULL, \
    DECISION_NAME             VARCHAR(120), \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERNAL_DECISION_MST ADD CONSTRAINT PK_ENT_INT_DISI_M PRIMARY KEY (DECISION_CD)
