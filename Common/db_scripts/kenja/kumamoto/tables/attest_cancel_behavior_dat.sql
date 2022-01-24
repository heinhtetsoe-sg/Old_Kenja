-- kanji=����
-- $Id: attest_cancel_behavior_dat.sql 59758 2018-04-16 14:21:49Z yamashiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_BEHAVIOR_DAT

CREATE TABLE ATTEST_CANCEL_BEHAVIOR_DAT  \
(   CANCEL_YEAR      VARCHAR(4)  NOT NULL,  \
    CANCEL_SEQ       SMALLINT    NOT NULL,  \
    CANCEL_STAFFCD   VARCHAR(10) NOT NULL,  \
    YEAR             VARCHAR(4)  NOT NULL,  \
    SCHREGNO         VARCHAR(8)  NOT NULL,  \
    DIV              VARCHAR(1)  NOT NULL,  \
    CODE             VARCHAR(2)  NOT NULL,  \
    ANNUAL           VARCHAR(2)  NOT NULL,  \
    RECORD           VARCHAR(1),  \
    REGISTERCD       VARCHAR(10),  \
    UPDATED          TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEST_CANCEL_BEHAVIOR_DAT  \
ADD CONSTRAINT PK_ATC_BEHAVIOR  \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, YEAR, SCHREGNO, DIV, CODE)
