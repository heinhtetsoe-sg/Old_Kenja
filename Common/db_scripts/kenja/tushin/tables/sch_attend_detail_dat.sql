-- kanji=����
-- $Id: sch_attend_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback


DROP TABLE SCH_ATTEND_DETAIL_DAT

CREATE TABLE SCH_ATTEND_DETAIL_DAT \
    (YEAR               VARCHAR(4) NOT NULL, \
     SCHREGNO           VARCHAR(8) NOT NULL, \
     EXECUTEDATE        DATE       NOT NULL, \
     CHAIRCD            VARCHAR(7) NOT NULL, \
     SEQ                VARCHAR(3) NOT NULL, \
     REMARK1            VARCHAR(90),  \
     REMARK2            VARCHAR(90),  \
     REMARK3            VARCHAR(90),  \
     REMARK4            VARCHAR(90),  \
     REMARK5            VARCHAR(90),  \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS
