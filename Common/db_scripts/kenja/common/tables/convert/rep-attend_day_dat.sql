-- kanji=����
-- $Id: 25dd2fb6a8ec026e50b1555ff2da7b8f8db06291 $
-- �з礱�ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP   TABLE ATTEND_DAY_DAT_OLD
RENAME TABLE ATTEND_DAY_DAT TO ATTEND_DAY_DAT_OLD

CREATE TABLE ATTEND_DAY_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    DI_CD           varchar(2) not null, \
    DI_REMARK       varchar(60), \
    YEAR            varchar(4) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ATTEND_DAY_DAT \
    SELECT \
        SCHREGNO, \
        ATTENDDATE, \
        DI_CD, \
        DI_REMARK, \
        YEAR, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ATTEND_DAY_DAT_OLD

ALTER TABLE ATTEND_DAY_DAT ADD CONSTRAINT PK_AT_DAY_DAT \
        PRIMARY KEY (SCHREGNO, ATTENDDATE, DI_CD)
