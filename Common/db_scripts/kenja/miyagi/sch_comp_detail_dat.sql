-- kanji=����
-- $Id: sch_comp_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table SCH_COMP_DETAIL_DAT

create table SCH_COMP_DETAIL_DAT \
(  \
        "YEAR"                  varchar(4) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "CLASSCD"               varchar(2) not null, \
        "SCHOOL_KIND"           varchar(2) not null, \
        "CURRICULUM_CD"         varchar(2) not null, \
        "SUBCLASSCD"            varchar(6) not null, \
        "KOUNIN"                varchar(1), \
        "ADD_CREDIT"            smallint, \
        "YOBI1"                 varchar(60), \
        "YOBI2"                 varchar(60), \
        "YOBI3"                 varchar(60), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCH_COMP_DETAIL_DAT  \
add constraint PK_SCH_COMP_DETAIL \
primary key  \
(YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
