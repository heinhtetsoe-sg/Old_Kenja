-- kanji=����
-- $Id: b783139436f366dad5ff88a4385725f3bc012191 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table ABSENCE_PATTERN_MST

create table ABSENCE_PATTERN_MST ( \
    YEAR                varchar(4)  not null, \
    PATTERNCD           varchar(2)  not null, \
    PATTERNCDNAME       varchar(75) not null, \
    BASEDATE            date        not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ABSENCE_PATTERN_MST add constraint PK_ABSENCE_PATT_M primary key (YEAR, PATTERNCD)
