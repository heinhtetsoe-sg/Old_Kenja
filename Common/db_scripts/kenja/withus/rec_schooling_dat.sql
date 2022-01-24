-- kanji=����
-- $Id: rec_schooling_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table REC_SCHOOLING_DAT

create table REC_SCHOOLING_DAT \
(  \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    SCHOOLING_TYPE  varchar(2) not null, \
    SEQ             smallint not null, \
    ATTENDDATETIME  timestamp, \
    GET_VALUE       smallint, \
    INPUT_TYPE      varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_SCHOOLING_DAT  \
add constraint PK_REC_SCHOOLING_D \
primary key  \
(YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCHOOLING_TYPE, SEQ)
