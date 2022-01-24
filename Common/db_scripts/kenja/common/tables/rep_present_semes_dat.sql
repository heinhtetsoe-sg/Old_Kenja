-- kanji=����
-- $Id: 523eb83d0a8b43d0d3a8ae9977e7d2008cd46f1a $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table REP_PRESENT_SEMES_DAT

create table REP_PRESENT_SEMES_DAT \
    (YEAR                 varchar(4) not null, \
     SEMESTER           varchar(1) not null, \
     CLASSCD              varchar(2) not null, \
     SCHOOL_KIND          varchar(2) not null, \
     CURRICULUM_CD        varchar(2) not null, \
     SUBCLASSCD           varchar(6) not null, \
     SCHREGNO             varchar(8) not null, \
     CHAIRCD              varchar(7), \
     REPORT_CNT           smallint, \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table REP_PRESENT_SEMES_DAT add constraint PK_REP_PRESENT_SEM primary key (YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)

