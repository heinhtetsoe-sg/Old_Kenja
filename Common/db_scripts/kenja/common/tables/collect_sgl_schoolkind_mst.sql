-- kanji=����
-- $Id: dc87ce727223e4bbd0bce0f642713b209d625725 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--SIGEL���֥ޥ���

drop table COLLECT_SGL_SCHOOLKIND_MST

create table COLLECT_SGL_SCHOOLKIND_MST \
( \
        "SGL_SCHOOLKIND"        varchar(1)  not null, \
        "SGL_SCHOOLKIND_NAME"   varchar(30) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_SCHOOLKIND_MST \
add constraint PK_C_SGL_SCHKIN_M \
primary key \
(SGL_SCHOOLKIND)
