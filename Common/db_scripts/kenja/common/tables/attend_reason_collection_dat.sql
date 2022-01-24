-- kanji=����
-- $Id: 9d0a96cbf850c23fb0a443bd732b00e6b2daec7a $
-- ������ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ATTEND_REASON_COLLECTION_DAT

create table ATTEND_REASON_COLLECTION_DAT ( \
    YEAR            varchar(4) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    COLLECTION_CD   varchar(2) not null, \
    SCHREGNO        varchar(8) not null, \
    ATTEND_REMARK   varchar(600), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_REASON_COLLECTION_DAT add constraint PK_ATT_RESON_COL_D \
        primary key (YEAR, SCHOOL_KIND, COLLECTION_CD, SCHREGNO)
