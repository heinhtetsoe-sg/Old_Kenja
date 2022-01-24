-- kanji=����
-- $Id: 16a6091645ed60121ed92afb5b8a97f44e08ec47 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table MOCK_DOCUMENT_DAT

create table MOCK_DOCUMENT_DAT ( \
    YEAR                varchar(4) not null, \
    MOCKCD              varchar(9) not null, \
    GRADE               varchar(2) not null, \
    COURSECD            varchar(1) not null, \
    MAJORCD             varchar(3) not null, \
    COURSECODE          varchar(4) not null, \
    MOCK_SUBCLASS_CD    varchar(6) not null, \
    FOOTNOTE            varchar(1050), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_DOCUMENT_DAT add constraint PK_MOCK_DOC_DAT \
      primary key (YEAR, MOCKCD, GRADE, COURSECD, MAJORCD, COURSECODE, MOCK_SUBCLASS_CD)
