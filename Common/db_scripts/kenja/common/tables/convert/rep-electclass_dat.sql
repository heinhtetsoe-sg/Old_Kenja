-- kanji=????
-- $Id: 9d1ad322488af5686ffd626e02dae7b9d919d5b8 $

DROP TABLE ELECTCLASS_DAT_OLD
RENAME TABLE ELECTCLASS_DAT TO ELECTCLASS_DAT_OLD
CREATE TABLE ELECTCLASS_DAT( \
    YEAR       VARCHAR(4)    NOT NULL, \
    GROUPCD    VARCHAR(4)    NOT NULL, \
    GROUPNAME  VARCHAR(9), \
    GROUPABBV  VARCHAR(6), \
    REMARK     VARCHAR(90), \
    SHOWORDER  SMALLINT, \
    REGISTERCD VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ELECTCLASS_DAT \
    SELECT \
        T2.YEAR, \
        T2.GROUPCD, \
        T1.GROUPNAME, \
        T1.GROUPABBV, \
        T1.REMARK, \
        T1.SHOWORDER, \
        T1.REGISTERCD, \
        T1.UPDATED \
    FROM \
        ELECTCLASS_MST T1, \
        ELECTCLASS_YDAT T2 \
    WHERE \
        T1.GROUPCD = T2.GROUPCD

ALTER TABLE ELECTCLASS_DAT ADD CONSTRAINT PK_ESC_DAT PRIMARY KEY (YEAR,GROUPCD)