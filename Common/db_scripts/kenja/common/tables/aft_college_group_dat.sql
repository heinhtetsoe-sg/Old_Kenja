-- kanji=����
-- $Id: 97092f11a7d5c303ce22b634e73bb5b9f1e75e67 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table AFT_COLLEGE_GROUP_DAT

create table AFT_COLLEGE_GROUP_DAT ( \
    YEAR                varchar(4)   not null, \
    COLLEGE_GRP_CD      varchar(2)   not null, \
    SCHOOL_CD           varchar(8)   not null, \
    REGISTERCD          varchar(10),           \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_COLLEGE_GROUP_DAT add constraint PK_AFT_COL_GRP_D primary key (YEAR, COLLEGE_GRP_CD, SCHOOL_CD)
