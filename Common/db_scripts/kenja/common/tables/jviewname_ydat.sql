-- kanji=����
-- $Id: dc961246725d2025c73657d30ad7c91dfbb1d00e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table JVIEWNAME_YDAT

create table JVIEWNAME_YDAT  \
(  \
        "YEAR"          varchar(4)  not null, \
        "VIEWCD"        varchar(4)  not null, \
        "REGISTERCD"    varchar(8),  \
        "UPDATED"       timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table JVIEWNAME_YDAT  \
add constraint pk_jviewname_ydat  \
primary key  \
( \
YEAR, \
VIEWCD \
)

