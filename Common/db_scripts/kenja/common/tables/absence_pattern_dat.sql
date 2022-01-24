-- kanji=����
-- $Id: 15a5747c8f39739fe430378426e82e473a8dc6b2 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table ABSENCE_PATTERN_DAT

create table ABSENCE_PATTERN_DAT ( \
    YEAR                varchar(4)  not null, \
    PATTERNCD           varchar(2)  not null, \
    ASSESSLEVEL         smallint    not null, \
    ASSESSMARK          varchar(3), \
    RATE                decimal (5,2), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ABSENCE_PATTERN_DAT add constraint PK_ABSENCE_PATT_D primary key (YEAR, PATTERNCD, ASSESSLEVEL)
