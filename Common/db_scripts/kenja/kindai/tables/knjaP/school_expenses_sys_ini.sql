-- kanji=����
-- $Id: school_expenses_sys_ini.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--SCHOOL_EXPENSES_SYS_INI	��Ǽ�⥷���ƥ�����ǡ���

DROP TABLE SCHOOL_EXPENSES_SYS_INI

CREATE TABLE SCHOOL_EXPENSES_SYS_INI \
( \
        "PROGRAMID"     VARCHAR(10) NOT NULL, \
        "DIV"           VARCHAR(4)  NOT NULL, \
        "VAR1"          VARCHAR(8), \
        "VAR2"          VARCHAR(8), \
        "INT1"          INTEGER, \
        "INT2"          INTEGER, \
        "DATE1"         DATE, \
        "DATE2"         DATE, \
        "REMARK"        VARCHAR(75), \
        "REGISTERCD"    VARCHAR(8), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHOOL_EXPENSES_SYS_INI \
ADD CONSTRAINT PK_SCREG_GRANT_DAT \
PRIMARY KEY \
(PROGRAMID,DIV)

INSERT INTO SCHOOL_EXPENSES_SYS_INI \
 (PROGRAMID,DIV,VAR1,REMARK) VALUES \
 ('KNJP000K','0001','02456','������ѳع�������')

INSERT INTO SCHOOL_EXPENSES_SYS_INI \
 (PROGRAMID,DIV,VAR1,REMARK) VALUES \
 ('KNJP050K','0001','1','P050K���ܥ��롼����������ι�����ǽ������ե饰')
