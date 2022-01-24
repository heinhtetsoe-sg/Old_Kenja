-- kanji=����
-- $Id: d96122f9b35018ade99ce0b36221a7e4b1c7bc70 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MOCK_CENTER_CLASS_MST

create table MOCK_CENTER_CLASS_MST ( \
    YEAR               varchar(4)  not null, \
    CENTER_CLASS_CD    varchar(2)  not null, \
    CLASS_NAME         varchar(60), \
    CLASS_ABBV         varchar(15), \
    CLASSCD            varchar(2), \
    SCHOOL_KIND        varchar(2), \
    CLASS_DIV          varchar(1), \
    PERFECT            smallint, \
    ALLOT_POINT        smallint, \
    REGISTERCD         varchar(10), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CENTER_CLASS_MST add constraint PK_MOCK_CENTER_C_M \
        primary key (YEAR, CENTER_CLASS_CD)
