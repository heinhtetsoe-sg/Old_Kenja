-- $Id: 19d159e01e7211b5905404cc2d1873b886cdad7c $

DROP TABLE GRD_ADDRESS_COMEBACK_DAT
CREATE TABLE GRD_ADDRESS_COMEBACK_DAT( \
    SCHREGNO        VARCHAR(8)    NOT NULL, \
    ISSUEDATE       DATE          NOT NULL, \
    COMEBACK_DATE   date          NOT NULL, \
    EXPIREDATE      DATE, \
    ZIPCD           VARCHAR(8), \
    AREACD          VARCHAR(2), \
    ADDR1           VARCHAR(150), \
    ADDR2           VARCHAR(150), \
    ADDR_FLG        VARCHAR(1), \
    ADDR1_ENG       VARCHAR(150), \
    ADDR2_ENG       VARCHAR(150), \
    TELNO           VARCHAR(14), \
    TELNO_MEMO      VARCHAR(24), \
    TELNO2          VARCHAR(14), \
    TELNO2_MEMO     VARCHAR(24), \
    FAXNO           VARCHAR(14), \
    EMAIL           VARCHAR(20), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRD_ADDRESS_COMEBACK_DAT ADD CONSTRAINT PK_GRD_ADDR_COME PRIMARY KEY (SCHREGNO, ISSUEDATE, COMEBACK_DATE)