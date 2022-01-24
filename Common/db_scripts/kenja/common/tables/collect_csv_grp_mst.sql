-- kanji=����
-- $Id: fc9f11f58d1b8167d018aeddc77536580e4bf173 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��ɼ�ǡ���
DROP TABLE COLLECT_CSV_GRP_MST

CREATE TABLE COLLECT_CSV_GRP_MST \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "GRP_CD"            varchar(3)  not null, \
        "GRP_NAME"          varchar(60), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_GRP_MST \
add constraint PK_COLL_CSV_GRPM \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, GRP_CD)
