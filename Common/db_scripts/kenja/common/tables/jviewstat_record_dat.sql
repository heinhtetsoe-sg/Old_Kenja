-- kanji=����
-- $Id: b595a84698170bb115bee3f2331d6d479ec33582 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table JVIEWSTAT_RECORD_DAT

create table JVIEWSTAT_RECORD_DAT(  \
    YEAR          varchar(4)  not null, \
    SEMESTER      varchar(1)  not null, \
    SCHREGNO      varchar(8)  not null, \
    CLASSCD       varchar(2)  not null, \
    SCHOOL_KIND   varchar(2)  not null, \
    CURRICULUM_CD varchar(2)  not null, \
    SUBCLASSCD    varchar(6)  not null, \
    VIEWCD        varchar(4)  not null, \
    STATUS        varchar(6),  \
    SCORE         smallint,  \
    REGISTERCD    varchar(10),  \
    UPDATED       timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table JVIEWSTAT_RECORD_DAT  \
add constraint pk_jviewstat_rec  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)
