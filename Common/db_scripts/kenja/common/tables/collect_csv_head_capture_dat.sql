-- kanji=����
-- $Id: f4e97b033aecf26b06f3c2c9c206befaa5d4cefd $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��ɼ�ǡ���
DROP TABLE COLLECT_CSV_HEAD_CAPTURE_DAT

CREATE TABLE COLLECT_CSV_HEAD_CAPTURE_DAT \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "ROW_NO"            varchar(3)  not null, \
        "HEAD_NAME"         varchar(60), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_HEAD_CAPTURE_DAT \
add constraint PK_COLL_CSV_CAPT \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, ROW_NO)
