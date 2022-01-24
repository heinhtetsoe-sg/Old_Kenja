-- kanji=����
-- $Id: 3b14d2f3b073d2ebfa58c3a41839ef2412887369 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��ɼ�ǡ���
DROP TABLE COLLECT_CSV_INFO_DAT

CREATE TABLE COLLECT_CSV_INFO_DAT \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "ROW_NO"            varchar(3)  not null, \
        "GRP_CD"            varchar(3), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_INFO_DAT \
add constraint PK_COLL_CSV_INFO \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, ROW_NO)
