-- kanji=����
-- $Id: comp_credits_pattern_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table COMP_CREDITS_PATTERN_DAT

create table COMP_CREDITS_PATTERN_DAT \
(  \
        "PATTERN_CD"            varchar(2) not null, \
        "CLASSCD"               varchar(2) not null, \
        "CURRICULUM_CD"         varchar(1) not null, \
        "SUBCLASSCD"            varchar(6) not null, \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMP_CREDITS_PATTERN_DAT  \
add constraint PK_COMP_CREDIT_P_D \
primary key  \
(PATTERN_CD, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
