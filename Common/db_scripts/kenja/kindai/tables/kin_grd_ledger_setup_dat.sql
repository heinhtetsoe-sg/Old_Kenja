--卒業生台帳設定データ
DROP TABLE KIN_GRD_LEDGER_SETUP_DAT

CREATE TABLE KIN_GRD_LEDGER_SETUP_DAT  \
(  \
        "SCHREGNO"                             VARCHAR(8)       NOT NULL, \
        "BIRTHDAY_FLG"                         VARCHAR(1), \
        "REGISTERCD"                           VARCHAR(8), \
        "UPDATED"                              TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KIN_GRD_LEDGER_SETUP_DAT  \
ADD CONSTRAINT PK_KIN_GRD_LED_SET  \
PRIMARY KEY  \
(SCHREGNO)

