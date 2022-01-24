-- kanji=����
-- $Id: a5526787c2d97aaaa11cbfd8449ff1097568dc70 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--�����زʸ���SIGEL�Ѵ��ơ��֥�

drop table COLLECT_SGL_COURSE_MAPPING_DAT

create table COLLECT_SGL_COURSE_MAPPING_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "GRADE"                 varchar(2)  not null, \
        "HR_CLASS"              varchar(3)  not null, \
        "SGL_SCHOOLKIND"        varchar(1)  , \
        "SGL_MAJORCD"           varchar(3)  , \
        "SGL_COURSECODE"        varchar(4)  , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_COURSE_MAPPING_DAT \
add constraint PK_C_SGL_COU_MAPD \
primary key \
(YEAR, GRADE, HR_CLASS)
