-- kanji=����
-- $Id: rec_commuting_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table REC_COMMUTING_DAT

create table REC_COMMUTING_DAT \
(  \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    ATTEND_DATE     date not null, \
    PERIODCD        varchar(2) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_COMMUTING_DAT  \
add constraint PK_REC_COMMUTING_D \
primary key  \
(YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ATTEND_DATE, PERIODCD)
