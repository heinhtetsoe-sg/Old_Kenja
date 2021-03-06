
DROP TABLE FURIKOMI_TAIYO_DAT

CREATE TABLE FURIKOMI_TAIYO_DAT( \
    S_KETTEI_DATE             DATE          NOT NULL, \
    E_KETTEI_DATE             DATE          NOT NULL, \
    SHIKIN_SHUBETSU           VARCHAR(1)    NOT NULL, \
    S_SHITEI_YM               VARCHAR(7)    NOT NULL, \
    E_SHITEI_YM               VARCHAR(7)    NOT NULL, \
    FURIKOMI_DATE             DATE          NOT NULL, \
    DATA_DIV                  VARCHAR(1)    NOT NULL, \
    SEQ                       INTEGER       NOT NULL, \
    ITEM                      VARCHAR(500), \
    REGISTERCD                VARCHAR(8), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE FURIKOMI_TAIYO_DAT ADD CONSTRAINT PK_KFURI_T_DAT PRIMARY KEY (S_KETTEI_DATE, E_KETTEI_DATE, SHIKIN_SHUBETSU, S_SHITEI_YM, E_SHITEI_YM, FURIKOMI_DATE, DATA_DIV, SEQ)

