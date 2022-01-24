-- kanji=����
-- $Id: 7a0470dafd4ea8c5ab615b58e24f3cac01628ffe $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ�����ײ��ܥޥ���

DROP TABLE LEVY_GROUP_MST

CREATE TABLE LEVY_GROUP_MST ( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "LEVY_GROUP_CD"     varchar(4) not null, \
        "LEVY_GROUP_NAME"   varchar(60), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_GROUP_MST ADD CONSTRAINT PK_LEVY_GROUP_MST PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, LEVY_GROUP_CD)
