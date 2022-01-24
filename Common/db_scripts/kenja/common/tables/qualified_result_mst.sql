-- kanji=����
-- $Id: 926952881ffadb3c60827b1f3d4125806d4a9e59 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table QUALIFIED_RESULT_MST

create table QUALIFIED_RESULT_MST \
(  \
    YEAR                VARCHAR(4)  not null, \
    QUALIFIED_CD        VARCHAR(4)  not null, \
    RESULT_CD           VARCHAR(4)  not null, \
    RESULT_NAME         VARCHAR(60), \
    RESULT_NAME_ABBV    VARCHAR(50), \
    CERT_FLG            VARCHAR(1), \
    LIMITED_PERIOD      INTEGER, \
    RESULT_LEVEL        INTEGER, \
    NOT_PRINT           VARCHAR(1), \
    SCORE               SMALLINT, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table QUALIFIED_RESULT_MST add constraint PK_QUALIFIED_RE_M \
primary key (YEAR, QUALIFIED_CD, RESULT_CD)
