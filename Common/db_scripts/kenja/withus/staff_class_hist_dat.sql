-- kanji=����
-- $Id: staff_class_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table STAFF_CLASS_HIST_DAT

create table STAFF_CLASS_HIST_DAT \
(  \
    YEAR            varchar(4) not null, \
    SEMESTER        varchar(1) not null, \
    GRADE           varchar(3) not null, \
    HR_CLASS        varchar(3) not null, \
    TR_DIV          varchar(1) not null, \
    FROM_DATE       date not null, \
    TO_DATE         date, \
    STAFFCD         varchar(8), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STAFF_CLASS_HIST_DAT add constraint PK_STF_CLASS_HIST \
primary key (YEAR, SEMESTER, GRADE, HR_CLASS, TR_DIV, FROM_DATE)
