-- kanji=����
-- $Id: efb8b515d192530e1f4dfc2941c678c339d57a22 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table SCHREG_SCHOOL_REFUSAL_DAT

create table SCHREG_SCHOOL_REFUSAL_DAT \
(  \
    YEAR                VARCHAR(4)  not null, \
    SCHREGNO            VARCHAR(8)  not null, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_SCHOOL_REFUSAL_DAT add constraint PK_SCHREG_SCHOOL_REFUSAL_D \
primary key (YEAR, SCHREGNO)
