-- $Id: 8d02d75abc4442c6aaa3788a5a4a05085fcded5c $

drop table STAFF_QUALIFIED_DAT

CREATE TABLE STAFF_QUALIFIED_DAT( \
    STAFFCD               VARCHAR(8)  NOT NULL, \
    SEQ                   VARCHAR(3)  NOT NULL, \
    QUALIFIED_CD          VARCHAR(120), \
    QUALIFIED_NAME        VARCHAR(150), \
    GET_DATE              DATE, \
    REGISTERCD            VARCHAR(8), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STAFF_QUALIFIED_DAT ADD CONSTRAINT PK_STAFF_QUALIFIED PRIMARY KEY (STAFFCD, SEQ)
