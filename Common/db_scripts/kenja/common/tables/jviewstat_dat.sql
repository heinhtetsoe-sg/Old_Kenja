-- kanji=����
-- $Id: 28bba3dc2241b51fb667407535fd1d439660928b $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table JVIEWSTAT_DAT

create table JVIEWSTAT_DAT  \
(  \
        "YEAR"          varchar(4)  not null, \
        "SEMESTER"      varchar(1)  not null, \
        "SCHREGNO"      varchar(8)  not null, \
        "VIEWCD"        varchar(4)  not null, \
        "STATUS"        varchar(1),  \
        "REGISTERCD"    varchar(8),  \
        "UPDATED"       timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table JVIEWSTAT_DAT  \
add constraint pk_jviewstat_dat  \
primary key  \
( \
YEAR, \
SEMESTER, \
SCHREGNO, \
VIEWCD \
)

