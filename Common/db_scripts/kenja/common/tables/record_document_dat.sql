-- kanji=����
-- $Id: 7f9b20ff656ec237164d6d355075ce534b027154 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table RECORD_DOCUMENT_DAT

create table RECORD_DOCUMENT_DAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    TESTKINDCD      VARCHAR(2) NOT NULL, \
    TESTITEMCD      VARCHAR(2) NOT NULL, \
    GRADE           VARCHAR(2) NOT NULL, \
    COURSECD        VARCHAR(1) NOT NULL, \
    MAJORCD         VARCHAR(3) NOT NULL, \
    COURSECODE      VARCHAR(4) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    FOOTNOTE        VARCHAR(1050), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table RECORD_DOCUMENT_DAT add constraint pk_record_doc_dat \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, GRADE, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
