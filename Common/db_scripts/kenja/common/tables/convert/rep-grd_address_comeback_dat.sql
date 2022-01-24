-- $Id: 6ed9b6bc1c6f835bf6c1110f1084602ad11dcf58 $

DROP TABLE GRD_ADDRESS_COMEBACK_DAT_OLD
RENAME TABLE GRD_ADDRESS_COMEBACK_DAT TO GRD_ADDRESS_COMEBACK_DAT_OLD
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

INSERT INTO GRD_ADDRESS_COMEBACK_DAT \
    SELECT \
        SCHREGNO        , \
        ISSUEDATE       , \
        COMEBACK_DATE   , \
        EXPIREDATE      , \
        ZIPCD           , \
        AREACD          , \
        ADDR1           , \
        ADDR2           , \
        ADDR_FLG        , \
        ADDR1_ENG       , \
        ADDR2_ENG       , \
        TELNO           , \
        cast(NULL as VARCHAR(24)) as TELNO_MEMO  , \
        cast(NULL as VARCHAR(14)) as TELNO2      , \
        cast(NULL as VARCHAR(24)) as TELNO2_MEMO , \
        FAXNO           , \
        EMAIL           , \
        REGISTERCD      , \
        UPDATED           \
    FROM \
        GRD_ADDRESS_COMEBACK_DAT_OLD
