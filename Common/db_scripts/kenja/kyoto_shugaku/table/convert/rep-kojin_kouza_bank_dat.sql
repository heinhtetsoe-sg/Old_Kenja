-- $Id: rep-kojin_kouza_bank_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_KOUZA_BANK_DAT_OLD
RENAME TABLE KOJIN_KOUZA_BANK_DAT TO KOJIN_KOUZA_BANK_DAT_OLD
CREATE TABLE KOJIN_KOUZA_BANK_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    TAISHOUSHA_DIV           VARCHAR(1)    NOT NULL, \
    KOUZA_DIV                VARCHAR(1)    NOT NULL, \
    S_DATE                   DATE          NOT NULL, \
    BANKCD                   VARCHAR(4)    NOT NULL, \
    BRANCHCD                 VARCHAR(3)    NOT NULL, \
    YOKIN_DIV                VARCHAR(1)    NOT NULL, \
    ACCOUNT_NO               VARCHAR(7)    NOT NULL, \
    BANK_MEIGI_SEI_KANA      VARCHAR(60)   NOT NULL, \
    BANK_MEIGI_MEI_KANA      VARCHAR(60)   NOT NULL, \
    BANK_MEIGI_SEI_NAME      VARCHAR(60), \
    BANK_MEIGI_MEI_NAME      VARCHAR(60), \
    ZIPCD                    VARCHAR(8), \
    ADDR1                    VARCHAR(150), \
    ADDR2                    VARCHAR(90), \
    TELNO1                   VARCHAR(14), \
    TELNO2                   VARCHAR(14), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO KOJIN_KOUZA_BANK_DAT \
    SELECT \
        KOJIN_NO, \
        '1' AS TAISHOUSHA_DIV, \
        KOUZA_DIV, \
        S_DATE, \
        BANKCD, \
        BRANCHCD, \
        YOKIN_DIV, \
        ACCOUNT_NO, \
        BANK_MEIGI_SEI_KANA, \
        BANK_MEIGI_MEI_KANA, \
        BANK_MEIGI_SEI_NAME, \
        BANK_MEIGI_MEI_NAME, \
        ZIPCD, \
        ADDR1, \
        ADDR2, \
        TELNO1, \
        TELNO2, \
        REGISTERCD, \
        UPDATED \
    FROM \
        KOJIN_KOUZA_BANK_DAT_OLD

ALTER TABLE KOJIN_KOUZA_BANK_DAT ADD CONSTRAINT PK_K_KO_BANK_DAT PRIMARY KEY (KOJIN_NO, TAISHOUSHA_DIV, KOUZA_DIV, S_DATE)