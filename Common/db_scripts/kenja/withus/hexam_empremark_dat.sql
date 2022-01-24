-- kanji=����
-- $Id: hexam_empremark_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HEXAM_EMPREMARK_DAT

create table HEXAM_EMPREMARK_DAT \
    (YEAR                   varchar(4) not null, \
     SCHREGNO               varchar(8) not null, \
     ANNUAL                 varchar(2), \
     JOBHUNT_REC            varchar(494), \
     JOBHUNT_ABSENCE        varchar(126), \
     REGISTERCD             varchar(8), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_EMPREMARK_DAT add constraint PK_HEXAM_EMP_DAT primary key (YEAR, SCHREGNO)


