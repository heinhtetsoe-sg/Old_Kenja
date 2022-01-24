-- kanji=����
-- $Id: 5617724a3840d76d53590664ca39d020da02aedf $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table OTHER_SYSTEM_MST

create table OTHER_SYSTEM_MST \
(  \
    SYSTEMID            VARCHAR(8)    not null, \
    SYSTEM_NAME         VARCHAR(120)  not null, \
    SYSTEM_NAME_ABBV    VARCHAR(120), \
    SHOWORDER           SMALLINT      not null, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table OTHER_SYSTEM_MST add constraint PK_OTHER_SYSTEM_M \
primary key (SYSTEMID)
