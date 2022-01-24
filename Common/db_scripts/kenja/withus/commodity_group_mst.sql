-- kanji=����
-- $Id: commodity_group_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table COMMODITY_GROUP_MST

create table COMMODITY_GROUP_MST \
(  \
        "GROUP_CD"                      varchar(2) not null, \
        "GROUP_NAME"                    varchar(150) not null, \
        "GROUP_ABBV"                    varchar(60), \
        "SHOWORDER"                     varchar(2), \
        "REGISTERCD"                    varchar(8), \
        "UPDATED"                       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMMODITY_GROUP_MST \
add constraint PK_COMMODITY_G_MST \
primary key  \
(GROUP_CD)
