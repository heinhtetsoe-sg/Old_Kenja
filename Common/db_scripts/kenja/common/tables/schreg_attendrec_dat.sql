-- kanji=����
-- $Id: 5dea31b121a08ca1989fa4ad610d4cdf2da6ee2e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table SCHREG_ATTENDREC_DAT

create table SCHREG_ATTENDREC_DAT \
    (SCHOOLCD             varchar(1) not null, \
     YEAR                 varchar(4) not null, \
     SCHREGNO             varchar(8) not null, \
     ANNUAL               varchar(2) not null, \
     SUMDATE              date, \
     CLASSDAYS            SMALLINT, \
     OFFDAYS              SMALLINT, \
     ABSENT               SMALLINT, \
     SUSPEND              SMALLINT, \
     MOURNING             SMALLINT, \
     ABROAD               SMALLINT, \
     REQUIREPRESENT       SMALLINT, \
     SICK                 SMALLINT, \
     ACCIDENTNOTICE       SMALLINT, \
     NOACCIDENTNOTICE     SMALLINT, \
     PRESENT              SMALLINT, \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_ATTENDREC_DAT \
add constraint PK_SCHREGATTENDREC \
primary key \
(SCHOOLCD, YEAR, SCHREGNO)

