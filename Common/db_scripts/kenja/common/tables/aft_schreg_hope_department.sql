-- kanji=����
-- $Id: 84b52bdf1ebc6938d4e2e09c4b4fed5165dfc439 $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table AFT_SCHREG_HOPE_DEPARTMENT

create table AFT_SCHREG_HOPE_DEPARTMENT ( \
    YEAR                     varchar(4) not null, \
    SCHREGNO                 varchar(8) not null, \
    HOPE_ORDER               varchar(2) not null, \
    DEPARTMENT_CD            varchar(2), \
    RECOMMENDATION_BASE_DIV  varchar(3), \
    DEPARTMENT_BASE_DIV      varchar(1), \
    REGISTERCD               varchar(10), \
    UPDATED                  timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_SCHREG_HOPE_DEPARTMENT add constraint PK_AFT_SCHREG_HOPE_DEPARTMENT primary key (YEAR, SCHREGNO, HOPE_ORDER)
