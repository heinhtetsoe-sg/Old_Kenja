-- kanji=����
-- $Id: 7e98f3e2fa4f33260af0d5327216eb18b08b9930 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ�����ײ��ܥޥ���

DROP TABLE LEVY_L_MST

CREATE TABLE LEVY_L_MST ( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           varchar(2) not null, \
        "LEVY_L_NAME"         varchar(90), \
        "LEVY_L_ABBV"         varchar(90), \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_L_MST ADD CONSTRAINT PK_LEVY_L_MST PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_L_CD)
