-- kanji=����
-- $Id: 8063ae9af52faf145221005aac95bb54655a527f $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ATTEND_SEMES_DETAIL_DAT

create table ATTEND_SEMES_DETAIL_DAT \
        (COPYCD             varchar(1)      not null, \
         YEAR               varchar(4)      not null, \
         MONTH              varchar(2)      not null, \	 
         SEMESTER           varchar(1)      not null, \
         SCHREGNO           varchar(8)      not null, \
         SEQ                varchar(3)      not null, \
         CNT                smallint, \
         VAL                varchar(2), \
         CNT_DECIMAL        decimal(5, 1), \
         REGISTERCD         varchar(8), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_SEMES_DETAIL_DAT add constraint pk_at_sem_det_dat primary key \
        (COPYCD, YEAR, MONTH, SEMESTER, SCHREGNO, SEQ)


