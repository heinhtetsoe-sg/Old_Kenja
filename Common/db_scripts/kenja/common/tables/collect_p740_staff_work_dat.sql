-- kanji=����
-- $Id: a3a22e25be97ed96ae3d9ce4d615be2af15d04f7 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
-- �����뿩������¸�ǡ�����KNJP740�ǻ��ѡ�
drop table COLLECT_P740_STAFF_WORK_DAT

create table COLLECT_P740_STAFF_WORK_DAT \
( \
        "YUCHO_CD"          varchar(4),  \
        "YUCHO_NAME"        varchar(45), \
        "BRANCHCD"          varchar(3),  \
        "ACCOUNTNO"         varchar(7),  \
        "STAFFCD"           varchar(8),  \
        "ACCOUNTNAME"       varchar(90), \
        "PLAN_MONEY"        varchar(10), \
        "YEAR_MONTH"        varchar(4),  \
        "SYORI_CD"          varchar(2),  \
        "HOJO"              varchar(2),  \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms
