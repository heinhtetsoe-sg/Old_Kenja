-- kanji=����
-- $Id: attend_correction_dat.sql 59752 2018-04-16 13:39:03Z yamashiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEND_CORRECTION_DAT

create table ATTEND_CORRECTION_DAT \
        (YEAR               varchar(4)      not null, \
         SEMESTER           varchar(1)      not null, \
         SCHREGNO           varchar(8)      not null, \
         LATEDETAIL         smallint, \
         EARLYDETAIL        smallint, \
         KEKKADETAIL        smallint, \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_CORRECTION_DAT add constraint pk_att_correc_dat primary key \
        (YEAR, SEMESTER, SCHREGNO)
