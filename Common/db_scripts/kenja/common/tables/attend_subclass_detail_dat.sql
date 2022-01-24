-- kanji=����
-- $Id: 1741b06ea3eb0cdc542caf03f96c2eb26b4204d6 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ATTEND_SUBCLASS_DETAIL_DAT

CREATE TABLE ATTEND_SUBCLASS_DETAIL_DAT( \
    COPYCD          VARCHAR(1) NOT NULL, \
    YEAR            VARCHAR(4) NOT NULL, \
    MONTH           VARCHAR(2) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    SCHREGNO        VARCHAR(8) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    SEQ             VARCHAR(3) NOT NULL, \
    CNT             SMALLINT, \
    VAL             VARCHAR(2), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE ATTEND_SUBCLASS_DETAIL_DAT add constraint pk_at_sub_det_dat primary key (COPYCD,YEAR,MONTH,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEQ)

