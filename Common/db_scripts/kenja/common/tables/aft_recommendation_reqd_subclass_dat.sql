-- kanji=����
-- $Id: 069546706c08b07a351411b75fa1a4e8977a35e8 $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table AFT_RECOMMENDATION_REQD_SUBCLASS_DAT

create table AFT_RECOMMENDATION_REQD_SUBCLASS_DAT ( \
    YEAR                   varchar(4) not null, \
    RECOMMENDATION_CD      varchar(4) not null, \
    COURSECD               varchar(1) not null, \
    MAJORCD                varchar(3) not null, \
    COURSECODE             varchar(4) not null, \
    CLASSCD                varchar(2) not null, \
    SCHOOL_KIND            varchar(2) not null, \
    CURRICULUM_CD          varchar(2) not null, \
    SUBCLASSCD             varchar(6) not null, \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_REQD_SUBCLASS_DAT add constraint PK_AFT_RECOMMENDATION_REQD_SUBCLASS_DAT \
primary key (YEAR, RECOMMENDATION_CD, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
