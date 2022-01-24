-- kanji=����
-- $Id: 00e20dd140c2ad827fb19f8e762f431d54af96d1 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ANOTHER_PRISCHOOL_HIST_DAT

CREATE TABLE ANOTHER_PRISCHOOL_HIST_DAT ( \
    YEAR                    VARCHAR(4)  NOT NULL, \
    SCHREGNO                VARCHAR(8)  NOT NULL, \
    PRISCHOOLCD             VARCHAR(7)  NOT NULL, \
    PRISCHOOL_CLASS_CD      VARCHAR(7)  NOT NULL, \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ANOTHER_PRISCHOOL_HIST_DAT ADD CONSTRAINT PK_ANO_PS_HIST PRIMARY KEY (YEAR, SCHREGNO, PRISCHOOLCD, PRISCHOOL_CLASS_CD)
