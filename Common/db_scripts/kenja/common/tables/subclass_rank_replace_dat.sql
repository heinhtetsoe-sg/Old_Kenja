-- kanji=����
-- $Id: fbd86ad14b02ffaf41acd057977e49b1c73bfa61 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table SUBCLASS_RANK_REPLACE_DAT

create table SUBCLASS_RANK_REPLACE_DAT ( \
    YEAR                      varchar(4) not null, \
    COMBINED_CLASSCD          varchar(2) not null, \
    COMBINED_SCHOOL_KIND      varchar(2) not null, \
    COMBINED_CURRICULUM_CD    varchar(2) not null, \
    COMBINED_SUBCLASSCD       varchar(6) not null, \
    ATTEND_CLASSCD            varchar(2) not null, \
    ATTEND_SCHOOL_KIND        varchar(2) not null, \
    ATTEND_CURRICULUM_CD      varchar(2) not null, \
    ATTEND_SUBCLASSCD         varchar(6) not null, \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_RANK_REPLACE_DAT add constraint PK_SUBRANKREP_DAT \
        primary key (YEAR, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
