-- kanji=����
-- $Id: 75dd49eeddad840de61536552b0cab88f5b82227 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table SCHREG_OTHER_SYSTEM_USER_DAT

create table SCHREG_OTHER_SYSTEM_USER_DAT \
(  \
    SYSTEMID            VARCHAR(8)      not null, \
    SCHREGNO            VARCHAR(8)      not null, \
    LOGINID             VARCHAR(26), \
    PASSWORD            VARCHAR(32), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_OTHER_SYSTEM_USER_DAT add constraint PK_SCH_OTHER_SY_D \
primary key (SYSTEMID, SCHREGNO)
