-- kanji=����
-- $Id: 7487852c44e6a88d8d532c16445e851df626bc1e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table SCHREG_QUALIFIED_TEST_DAT

create table SCHREG_QUALIFIED_TEST_DAT \
(  \
    YEAR                VARCHAR(4)  not null, \
    SCHREGNO            VARCHAR(8)  not null, \
    TEST_DATE           DATE        not null, \
    QUALIFIED_CD        VARCHAR(4)  not null, \
    TEST_CD             VARCHAR(4)  not null, \
    RESULT_CD           VARCHAR(4), \
    LIMITED_DATE        DATE, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_QUALIFIED_TEST_DAT add constraint PK_SCH_QUAL_TES_D \
primary key (YEAR, SCHREGNO, TEST_DATE, QUALIFIED_CD, TEST_CD)
