-- kanji=����
-- $Id: fdcf22ebedbebd0ae87691828ae357a684d126cd $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table MOCK_TOTAL_SUBCLASS_DAT

create table MOCK_TOTAL_SUBCLASS_DAT ( \
    YEAR             varchar(4) not null, \
    MOCKCD           varchar(9) not null, \
    COURSECD         varchar(1) not null, \
    MAJORCD          varchar(3) not null, \
    COURSECODE       varchar(4) not null, \
    MOCK_SUBCLASS_CD varchar(6) , \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_TOTAL_SUBCLASS_DAT add constraint PK_MOCK_TOTAL_SUBCLASS_D primary key (YEAR, MOCKCD, COURSECD, MAJORCD, COURSECODE)
