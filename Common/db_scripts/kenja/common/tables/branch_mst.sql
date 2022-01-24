-- kanji=����
-- $Id: a489de2d5311470e6f1b8ea36ce46b667946a6c7 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table BRANCH_MST

create table BRANCH_MST ( \
    BRANCHCD            varchar(2)  not null, \
    BRANCHNAME          varchar(75), \
    ABBV                varchar(75), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table BRANCH_MST add constraint PK_BRANCH_MST primary key (BRANCHCD)
