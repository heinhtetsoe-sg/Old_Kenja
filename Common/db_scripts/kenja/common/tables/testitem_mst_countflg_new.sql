-- kanji=����
-- $Id: 6401018a3fee67b2e66f9d6ad542a35aab301ebf $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
DROP TABLE TESTITEM_MST_COUNTFLG_NEW

CREATE TABLE TESTITEM_MST_COUNTFLG_NEW( \
    YEAR            VARCHAR(4)    NOT NULL, \
    SEMESTER        VARCHAR(1)    NOT NULL, \
    TESTKINDCD      VARCHAR(2)    NOT NULL, \
    TESTITEMCD      VARCHAR(2)    NOT NULL, \
    TESTITEMNAME    VARCHAR(30), \
    COUNTFLG        VARCHAR(1), \
    SEMESTER_DETAIL VARCHAR(1), \
    TEST_START_DATE DATE, \
    TEST_END_DATE   DATE, \
    REGISTERCD      varchar(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE TESTITEM_MST_COUNTFLG_NEW ADD CONSTRAINT PK_TESTITEM_M_CF_N \
      PRIMARY KEY (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD)
