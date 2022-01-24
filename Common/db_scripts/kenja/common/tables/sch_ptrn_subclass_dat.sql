-- kanji=����
-- $Id: dd77d0c5ee38b0119cfee50e3c5aca36862b4eff $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table SCH_PTRN_SUBCLASS_DAT

create table SCH_PTRN_SUBCLASS_DAT ( \
    YEAR            varchar(4)  not null, \
    SEQ             smallint    not null, \
    WEEK_CD         varchar(1)  not null, \
    PERIODCD        varchar(1)  not null, \
    STAFFCD         varchar(10) not null, \
    GRADE           varchar(2)  not null, \
    HR_CLASS        varchar(3)  not null, \
    CLASSCD         varchar(2)  not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    CURRICULUM_CD   varchar(2)  not null, \
    SUBCLASSCD      varchar(6)  not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCH_PTRN_SUBCLASS_DAT add constraint PK_SCH_PTRN_SUBD primary key (YEAR, SEQ, WEEK_CD, PERIODCD, STAFFCD, GRADE, HR_CLASS)
