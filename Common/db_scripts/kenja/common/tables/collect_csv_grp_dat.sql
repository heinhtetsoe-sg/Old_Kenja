-- kanji=����
-- $Id: 4019bc4ce9d58eab78fd5e071541262407df243d $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��ɼ�ǡ���
DROP TABLE COLLECT_CSV_GRP_DAT

CREATE TABLE COLLECT_CSV_GRP_DAT \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "GRP_CD"            varchar(3)  not null, \
        "COLLECT_L_CD"      varchar(2)  not null, \
        "COLLECT_M_CD"      varchar(2)  not null, \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_GRP_DAT \
add constraint PK_COLL_CSV_GRPD \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, GRP_CD, COLLECT_L_CD, COLLECT_M_CD)
