-- $Id: 7c867dd1e54ecf2d1f7137e2a94f40e40b886e19 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ENTEXAM_INTERNAL_DECISION_YDAT
CREATE TABLE ENTEXAM_INTERNAL_DECISION_YDAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    DECISION_CD               VARCHAR(1)    NOT NULL, \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERNAL_DECISION_YDAT ADD CONSTRAINT PK_ENT_INT_DISI_Y PRIMARY KEY (ENTEXAMYEAR, DECISION_CD)
