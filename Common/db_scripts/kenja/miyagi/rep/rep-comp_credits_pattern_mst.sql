-- kanji=����
-- $Id: rep-comp_credits_pattern_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table COMP_CREDITS_PATTERN_MST_OLD
create table COMP_CREDITS_PATTERN_MST_OLD like COMP_CREDITS_PATTERN_MST
insert into COMP_CREDITS_PATTERN_MST_OLD select * from COMP_CREDITS_PATTERN_MST

drop table COMP_CREDITS_PATTERN_MST

create table COMP_CREDITS_PATTERN_MST \
(  \
        "YEAR"                  varchar(4) not null, \
        "PATTERN_CD"            varchar(2) not null, \
        "PATTERN_NAME"          varchar(90), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMP_CREDITS_PATTERN_MST  \
add constraint PK_COMP_CREDIT_P_M \
primary key  \
(YEAR, PATTERN_CD)

insert into \
COMP_CREDITS_PATTERN_MST \
SELECT \
    T1.YEAR, \
    T2.PATTERN_CD, \
    T2.PATTERN_NAME, \
    T2.REGISTERCD, \
    T2.UPDATED \
FROM \
    SCHOOL_MST T1, \
    COMP_CREDITS_PATTERN_MST_OLD T2
