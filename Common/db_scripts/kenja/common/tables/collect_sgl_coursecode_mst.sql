-- kanji=����
-- $Id: 3aa1f144c505967f09327934c467294e4638e1a0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--SIGEL�زʥޥ���

drop table COLLECT_SGL_COURSECODE_MST

create table COLLECT_SGL_COURSECODE_MST \
( \
        "SGL_SCHOOLKIND"        varchar(1)  not null, \
        "SGL_MAJORCD"           varchar(3)  not null, \
        "SGL_COURSECODE"        varchar(4)  not null, \
        "SGL_COURSECODE_NAME"   varchar(30) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_COURSECODE_MST \
add constraint PK_C_SGL_COCODE_M \
primary key \
(SGL_SCHOOLKIND, SGL_MAJORCD, SGL_COURSECODE)
