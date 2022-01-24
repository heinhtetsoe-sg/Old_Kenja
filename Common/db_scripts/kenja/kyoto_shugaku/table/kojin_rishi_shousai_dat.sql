-- $Id: kojin_rishi_shousai_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_RISHI_SHOUSAI_DAT
CREATE TABLE KOJIN_RISHI_SHOUSAI_DAT( \
    SHUUGAKU_NO                 VARCHAR(7)  NOT NULL, \
    SHINSEI_YEAR                VARCHAR(4)  NOT NULL, \
    KOUFU_SEQ                   VARCHAR(2)  NOT NULL, \
    KOUBAN                      VARCHAR(2)  NOT NULL, \
    SHIKIN_SHUBETSU             VARCHAR(1)  NOT NULL, \
    KOJIN_NO                    VARCHAR(7)  NOT NULL, \
    HENKANZUMI_DATE             DATE        NOT NULL, \
    RISHI_GK                    INT         NOT NULL, \
    KARIIRE_ZAN_GK              INT         NOT NULL, \
    S_KEISAN_DATE               DATE        NOT NULL, \
    E_KEISAN_DATE               DATE, \
    KEISAN_NISUU                VARCHAR(3), \
    SHIHARAI_FUKA_FLG           VARCHAR(4), \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_RISHI_SHOUSAI_DAT ADD CONSTRAINT PK_K_RISHI_SH_DAT PRIMARY KEY (SHUUGAKU_NO, SHINSEI_YEAR, KOUFU_SEQ, KOUBAN)