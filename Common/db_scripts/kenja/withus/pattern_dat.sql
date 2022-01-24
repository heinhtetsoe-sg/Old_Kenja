-- kanji=����
-- $Id: pattern_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table PATTERN_DAT

create table PATTERN_DAT \
(  \
    PATTERN_CD     varchar(3) not null, \
    LINE_NO        varchar(2) not null, \
    COMMODITY_CD   varchar(5), \
    AMOUNT         varchar(2), \
    PRIORITY_LEVEL varchar(2), \
    AMOUNT_DIV     varchar(1), \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PATTERN_DAT  \
add constraint PK_PATTERN_DAT  \
primary key  \
(PATTERN_CD, LINE_NO)
