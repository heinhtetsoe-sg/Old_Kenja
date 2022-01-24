-- $Id: 5f813e633f073585bf9622741ae7e9d0d6b71d0d $

DROP TABLE GUARDIAN_ADDRESS_DAT
CREATE TABLE GUARDIAN_ADDRESS_DAT( \
    SCHREGNO       VARCHAR(8)    NOT NULL, \
    ISSUEDATE      DATE          NOT NULL, \
    EXPIREDATE     DATE, \
    GUARD_ZIPCD    VARCHAR(8), \
    GUARD_ADDR1    VARCHAR(150), \
    GUARD_ADDR2    VARCHAR(150), \
    GUARD_ADDR_FLG VARCHAR(1), \
    GUARD_TELNO    VARCHAR(14), \
    GUARD_TELNO2   VARCHAR(14), \
    GUARD_FAXNO    VARCHAR(14), \
    GUARD_E_MAIL   VARCHAR(50), \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GUARDIAN_ADDRESS_DAT ADD CONSTRAINT PK_GUARDIAN_ADD PRIMARY KEY (SCHREGNO,ISSUEDATE)