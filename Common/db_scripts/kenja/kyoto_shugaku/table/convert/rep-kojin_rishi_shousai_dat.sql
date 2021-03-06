-- $Id: rep-kojin_rishi_shousai_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_RISHI_SHOUSAI_DAT_OLD
RENAME TABLE KOJIN_RISHI_SHOUSAI_DAT TO KOJIN_RISHI_SHOUSAI_DAT_OLD
CREATE TABLE KOJIN_RISHI_SHOUSAI_DAT( \
    SHUUGAKU_NO                 VARCHAR(7)  NOT NULL, \
    SHINSEI_YEAR                VARCHAR(4)  NOT NULL, \
    KOUFU_SEQ                   VARCHAR(2)  NOT NULL, \
    KOUBAN                      VARCHAR(2)  NOT NULL, \
    SHIKIN_SHUBETSU             VARCHAR(1)  NOT NULL, \
    KOJIN_NO                    VARCHAR(7)  NOT NULL, \
    HENKANZUMI_DATE             DATE, \
    RISHI_GK                    INT, \
    KARIIRE_ZAN_GK              INT, \
    S_KEISAN_DATE               DATE, \
    E_KEISAN_DATE               DATE, \
    KEISAN_NISUU                VARCHAR(3), \
    SHIHARAI_FUKA_FLG           VARCHAR(4), \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO KOJIN_RISHI_SHOUSAI_DAT \
    SELECT \
        SHUUGAKU_NO, \
        SHINSEI_YEAR, \
        KOUFU_SEQ, \
        right(rtrim('0'|| KOUBAN),2) AS KOUBAN, \
        SHIKIN_SHUBETSU, \
        KOJIN_NO, \
        HENKANZUMI_DATE, \
        RISHI_GK, \
        KARIIRE_ZAN_GK, \
        S_KEISAN_DATE, \
        E_KEISAN_DATE, \
        right(rtrim('00'|| KEISAN_NISUU),3) AS KEISAN_NISUU, \
        SHIHARAI_FUKA_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        KOJIN_RISHI_SHOUSAI_DAT_OLD

ALTER TABLE KOJIN_RISHI_SHOUSAI_DAT ADD CONSTRAINT PK_K_RISHI_SH_DAT PRIMARY KEY (SHUUGAKU_NO, SHINSEI_YEAR, KOUFU_SEQ, KOUBAN)
