-- kanji=����
-- $Id: 4982bcf0de73e071705d900626221d32dbe92ce2 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HEALTH_SPORT_PRINT_HIST_DAT

create table HEALTH_SPORT_PRINT_HIST_DAT \
    (YEAR               varchar(4) not null, \
     SCHOOLCD           varchar(12) not null, \
     SCHOOL_KIND        varchar(2) not null, \
     SCHREGNO           varchar(8) not null, \
     SEQ                smallint not null, \
     SEND_TO1           varchar(120), \
     SEND_TO2           varchar(120), \
     SEND_DATE          date, \
     REMARK1            varchar(120), \
     REMARK2            varchar(120), \
     REMARK3            varchar(120), \
     REMARK4            varchar(120), \
     REMARK5            varchar(120), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEALTH_SPORT_PRINT_HIST_DAT add constraint PK_HEALTH_SPHIST primary key (YEAR, SCHOOLCD, SCHOOL_KIND, SCHREGNO, SEQ)
