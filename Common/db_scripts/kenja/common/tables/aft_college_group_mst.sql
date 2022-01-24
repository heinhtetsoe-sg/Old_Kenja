-- kanji=����
-- $Id: 1ed15f49f498b924ce3dec28faa9c5e214363797 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table AFT_COLLEGE_GROUP_MST

create table AFT_COLLEGE_GROUP_MST ( \
    YEAR                varchar(4)   not null, \
    COLLEGE_GRP_CD      varchar(2)   not null, \
    COLLEGE_GRP_NAME    varchar(60)          , \
    REGISTERCD          varchar(10)          , \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_COLLEGE_GROUP_MST add constraint PK_AFT_COL_GRP_M primary key (YEAR, COLLEGE_GRP_CD)
