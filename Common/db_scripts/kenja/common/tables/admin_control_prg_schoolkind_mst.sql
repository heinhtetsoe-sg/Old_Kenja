-- kanji=����
-- $Id: 858271c098dcb89baa2b08a86042fed4aff19156 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table ADMIN_CONTROL_PRG_SCHOOLKIND_MST

create table ADMIN_CONTROL_PRG_SCHOOLKIND_MST ( \
    SCHOOL_KIND         varchar(2) not null, \
    PROGRAMID           varchar(20) not null, \
    SELECT_SCHOOL_KIND  varchar(2) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_PRG_SCHOOLKIND_MST add constraint PK_CTRL_SCHKIND_M primary key (SCHOOL_KIND, PROGRAMID, SELECT_SCHOOL_KIND)
