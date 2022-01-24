-- $Id: 9af0e8bf975d3818d8601b9fa94609bf52e80211 $

DROP TABLE GRD_GUARDIAN2_ADDRESS_DAT_OLD
RENAME TABLE GRD_GUARDIAN2_ADDRESS_DAT TO GRD_GUARDIAN2_ADDRESS_DAT_OLD
CREATE TABLE GRD_GUARDIAN2_ADDRESS_DAT( \
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

ALTER TABLE GRD_GUARDIAN2_ADDRESS_DAT ADD CONSTRAINT PK_GRD_GUARDIAN2_A PRIMARY KEY (SCHREGNO,ISSUEDATE)

INSERT INTO GRD_GUARDIAN2_ADDRESS_DAT \
    SELECT \
        SCHREGNO, \
        ISSUEDATE, \
        EXPIREDATE, \
        GUARD_ZIPCD, \
        GUARD_ADDR1, \
        GUARD_ADDR2, \
        GUARD_ADDR_FLG, \
        GUARD_TELNO, \
        CAST(NULL AS VARCHAR(14)) AS GUARD_TELNO2, \
        GUARD_FAXNO, \
        GUARD_E_MAIL, \
        REGISTERCD, \
        UPDATED \
    FROM \
        GRD_GUARDIAN2_ADDRESS_DAT_OLD
