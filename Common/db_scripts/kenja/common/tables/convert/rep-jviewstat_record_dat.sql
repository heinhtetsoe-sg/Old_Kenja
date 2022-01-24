-- kanji=����
-- $Id: cbe1d1216fe2304e87e910b44d1866dafa2cfd14 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table JVIEWSTAT_RECORD_DAT_OLD
rename table JVIEWSTAT_RECORD_DAT TO JVIEWSTAT_RECORD_DAT_OLD

create table JVIEWSTAT_RECORD_DAT( \
    YEAR          varchar(4)  not null, \
    SEMESTER      varchar(1)  not null, \
    SCHREGNO      varchar(8)  not null, \
    CLASSCD       varchar(2)  not null, \
    SCHOOL_KIND   varchar(2)  not null, \
    CURRICULUM_CD varchar(2)  not null, \
    SUBCLASSCD    varchar(6)  not null, \
    VIEWCD        varchar(4)  not null, \
    STATUS        varchar(6), \
    SCORE         smallint, \
    REGISTERCD    varchar(10), \
    UPDATED       timestamp default current timestamp  \
    ) in usr1dms index in idx1dms

insert into JVIEWSTAT_RECORD_DAT \
    select \
        YEAR, \
        SEMESTER, \
        SCHREGNO, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        VIEWCD, \
        STATUS, \
        cast(null as smallint) as SCORE, \
        REGISTERCD, \
        UPDATED \
     from \
         JVIEWSTAT_RECORD_DAT_OLD

alter table JVIEWSTAT_RECORD_DAT  \
add constraint pk_jviewstat_rec  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)
