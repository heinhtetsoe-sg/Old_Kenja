-- kanji=����
-- $Id: a5b16c1ea41cd1e9343a9e57a38a9915b608e1c7 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--SIGEL�����ޥ���

drop table COLLECT_SGL_MAJORCD_MST

create table COLLECT_SGL_MAJORCD_MST \
( \
        "SGL_SCHOOLKIND"        varchar(1)  not null, \
        "SGL_MAJORCD"           varchar(3)  not null, \
        "SGL_MAJORCD_NAME"      varchar(30) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_MAJORCD_MST \
add constraint PK_C_SGL_MAJRCD_M \
primary key \
(SGL_SCHOOLKIND, SGL_MAJORCD)
