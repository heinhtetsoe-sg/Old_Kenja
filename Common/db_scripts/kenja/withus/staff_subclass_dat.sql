-- kanji=����
-- $Id: staff_subclass_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table STAFF_SUBCLASS_DAT

create table STAFF_SUBCLASS_DAT \
(  \
    YEAR            varchar(4) not null, \
    STAFFCD         varchar(8) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STAFF_SUBCLASS_DAT  \
add constraint PK_STAFF_SUBCLASS \
primary key  \
(YEAR, STAFFCD, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
