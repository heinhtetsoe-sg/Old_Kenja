-- kanji=����
-- $Id: attest_cancel_schreg_attendrec_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_SCHREG_ATTENDREC_DAT

CREATE TABLE ATTEST_CANCEL_SCHREG_ATTENDREC_DAT  \
(   CANCEL_YEAR      VARCHAR(4) NOT NULL , \
    CANCEL_SEQ       SMALLINT   NOT NULL , \
    CANCEL_STAFFCD   VARCHAR(8) NOT NULL , \
    SCHOOLCD         VARCHAR(1) NOT NULL , \
    YEAR             VARCHAR(4) NOT NULL , \
    SCHREGNO         VARCHAR(8) NOT NULL , \
    ANNUAL           VARCHAR(2) NOT NULL , \
    SUMDATE          DATE , \
    CLASSDAYS        SMALLINT  , \
    OFFDAYS          SMALLINT  , \
    ABSENT           SMALLINT , \
    SUSPEND          SMALLINT , \
    MOURNING         SMALLINT , \
    ABROAD           SMALLINT , \
    REQUIREPRESENT   SMALLINT , \
    SICK             SMALLINT , \
    ACCIDENTNOTICE   SMALLINT , \
    NOACCIDENTNOTICE SMALLINT , \
    PRESENT          SMALLINT , \
    REGISTERCD       VARCHAR(8)  , \
    UPDATED          TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE ATTEST_CANCEL_SCHREG_ATTENDREC_DAT  \
ADD CONSTRAINT PK_ATC_SCHREGATEND \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, SCHOOLCD, YEAR, SCHREGNO)
