-- kanji=����
-- $Id: 958872fdc2c7118b48ed86a3f4b494e0e5ecb408 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table FINSCHOOL_DETAIL_MST

create table FINSCHOOL_DETAIL_MST \
(  \
    FINSCHOOLCD     varchar(12)  not null, \
    FINSCHOOL_SEQ   varchar(3)  not null, \
    REMARK1         varchar(90), \
    REMARK2         varchar(90), \
    REMARK3         varchar(90), \
    REMARK4         varchar(90), \
    REMARK5         varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table FINSCHOOL_DETAIL_MST add constraint PK_FINSCHOOL_DT_M \
primary key (FINSCHOOLCD, FINSCHOOL_SEQ)
