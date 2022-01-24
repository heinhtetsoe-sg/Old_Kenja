-- kanji=����
-- $Id: add401980778760789fe2104b50d942965d49f0b $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��������������ޥ���

drop TABLE LEVY_TAX_MST

create TABLE LEVY_TAX_MST ( \
        "YEAR"              varchar(4) not null, \
        "TAX_CD"            varchar(3) not null, \
        "DATE_FROM"         date       not null, \
        "DATE_TO"           date, \
        "TAX_VALUE"         varchar(10), \
        "TAX_SUMMARY"       varchar(90), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_TAX_MST add CONSTRAINT PK_TAX_MST primary key (YEAR, TAX_CD, DATE_FROM)
