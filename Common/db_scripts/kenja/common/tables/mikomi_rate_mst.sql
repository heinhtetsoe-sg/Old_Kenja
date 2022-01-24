-- kanji=����
-- $Id: 5273d3f33eaa5427e8c7ad9809a8a96b20e4a54b $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MIKOMI_RATE_MST

create table MIKOMI_RATE_MST ( \
    YEAR                    varchar(4) not null, \
    SCHOOL_KIND             varchar(2) not null, \
    GRADE                   varchar(2) not null, \
    MIKOMI_RATE             smallint , \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MIKOMI_RATE_MST add constraint PK_MIKOMI_RATE_MST \
        primary key (YEAR, SCHOOL_KIND, GRADE)
