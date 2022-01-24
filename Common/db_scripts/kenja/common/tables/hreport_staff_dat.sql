-- kanji=����
-- $Id: ed6b48e6bbf52e43875357f2b7bda674c1f2759b $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��Ͽ��ԥǡ���
drop table HREPORT_STAFF_DAT

create table HREPORT_STAFF_DAT \
( \
        "YEAR"          varchar(4)  not null, \
        "SEMESTER"      varchar(1)  not null, \
        "SCHREGNO"      varchar(8)  not null, \
        "SEQ"           varchar(3)  not null,  \
        "STAFFCD"       varchar(10),  \
        "REGISTERCD"    varchar(10), \
        "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_STAFF_DAT add constraint PK_HREPORT_STAFF_DAT primary key (YEAR, SEMESTER, SCHREGNO, SEQ)
