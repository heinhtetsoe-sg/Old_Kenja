-- kanji=����
-- $Id: comp_credits_pattern_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table COMP_CREDITS_PATTERN_MST

create table COMP_CREDITS_PATTERN_MST \
(  \
        "PATTERN_CD"            varchar(2) not null, \
        "PATTERN_NAME"          varchar(90), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMP_CREDITS_PATTERN_MST  \
add constraint PK_COMP_CREDIT_P_M \
primary key  \
(PATTERN_CD)
