-- kanji=����
-- $Id: 02c943a0eedf0c464fe759e73e31fd6ca7a0146d $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CHAIR_GROUP_DAT

create table CHAIR_GROUP_DAT \
(  \
        "YEAR"              varchar(4) not null, \
        "SEMESTER"          varchar(1) not null, \
        "CHAIR_GROUP_CD"    varchar(6) not null, \
        "TESTKINDCD"        varchar(2) not null, \
        "TESTITEMCD"        varchar(2) not null, \
        "CHAIRCD"           varchar(7) not null, \
        "REGISTERCD"        varchar(8), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CHAIR_GROUP_DAT  \
add constraint PK_CHAIR_GROUP_DAT \
primary key  \
(YEAR, SEMESTER, CHAIR_GROUP_CD, TESTKINDCD, TESTITEMCD, CHAIRCD)
