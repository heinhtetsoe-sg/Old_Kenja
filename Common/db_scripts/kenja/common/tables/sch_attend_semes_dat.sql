-- kanji=����
-- $Id: 98144acd80baccf490dfa22226c4eb2239beec30 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback


drop table SCH_ATTEND_SEMES_DAT

create table SCH_ATTEND_SEMES_DAT \
    (YEAR               varchar(4) not null, \
     SEMESTER           varchar(1) not null, \
     SCHREGNO           varchar(8) not null, \
     CHAIRCD            varchar(7) not null, \
     SCHOOLING_CNT      smallint, \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) IN USR1DMS INDEX IN IDX1DMS

alter table SCH_ATTEND_SEMES_DAT add constraint PK_SCH_ATTEND_SEM primary key (YEAR, SEMESTER, SCHREGNO, CHAIRCD)
