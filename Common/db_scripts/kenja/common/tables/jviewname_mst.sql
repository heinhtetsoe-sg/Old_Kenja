-- kanji=����
-- $Id: 80fc9b7e8f8126439ddd2e9a9fdb8e0c7cf8be38 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table JVIEWNAME_MST

create table JVIEWNAME_MST  \
(  \
        "VIEWCD"        varchar(4)  not null, \
        "VIEWNAME"      varchar(75), \
        "SHOWORDER"     smallint, \
        "REGISTERCD"    varchar(8),  \
        "UPDATED"       timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table JVIEWNAME_MST  \
add constraint pk_jviewname_mst  \
primary key  \
( \
VIEWCD \
)

