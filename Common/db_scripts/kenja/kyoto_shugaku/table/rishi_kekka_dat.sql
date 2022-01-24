-- $Id: rishi_kekka_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE RISHI_KEKKA_DAT
CREATE TABLE RISHI_KEKKA_DAT( \
    SAKUSEI_DATE                DATE          NOT NULL, \
    BANKCD                      VARCHAR(4)    NOT NULL, \
    SHORI_NO                    VARCHAR(4)    NOT NULL, \
    SHORI_EDABAN                VARCHAR(3)    NOT NULL, \
    SHORI_YEAR                  VARCHAR(4)    NOT NULL, \
    SHINSEI_NAME                VARCHAR(90), \
    SHINSEI_BIRTHDAY            DATE, \
    SHINSEI_ZIPCD               VARCHAR(8), \
    SHINSEI_ADDR                VARCHAR(150), \
    SHINSEI_TELNO               VARCHAR(11), \
    YUUSHI_COURSE_DIV           VARCHAR(1), \
    YUUSHI_GK                   INT           NOT NULL, \
    YUUSHI_RITSU                DECIMAL(6, 3) NOT NULL, \
    KARIIRE_DATE                DATE, \
    SHIKIN_SHUBETSU             VARCHAR(1), \
    HENSAI1_DATE                DATE, \
    RISHI1_GK                   INT           NOT NULL, \
    KARIIRE_ZAN1_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN1_DATE       DATE, \
    E_RISOKU_KEISAN1_DATE       DATE, \
    RISOKU_KEISAN1_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI2_DATE                DATE, \
    RISHI2_GK                   INT           NOT NULL, \
    KARIIRE_ZAN2_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN2_DATE       DATE, \
    E_RISOKU_KEISAN2_DATE       DATE, \
    RISOKU_KEISAN2_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI3_DATE                DATE, \
    RISHI3_GK                   INT           NOT NULL, \
    KARIIRE_ZAN3_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN3_DATE       DATE, \
    E_RISOKU_KEISAN3_DATE       DATE, \
    RISOKU_KEISAN3_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI4_DATE                DATE, \
    RISHI4_GK                   INT           NOT NULL, \
    KARIIRE_ZAN4_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN4_DATE       DATE, \
    E_RISOKU_KEISAN4_DATE       DATE, \
    RISOKU_KEISAN4_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI5_DATE                DATE, \
    RISHI5_GK                   INT           NOT NULL, \
    KARIIRE_ZAN5_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN5_DATE       DATE, \
    E_RISOKU_KEISAN5_DATE       DATE, \
    RISOKU_KEISAN5_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI6_DATE                DATE, \
    RISHI6_GK                   INT           NOT NULL, \
    KARIIRE_ZAN6_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN6_DATE       DATE, \
    E_RISOKU_KEISAN6_DATE       DATE, \
    RISOKU_KEISAN6_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI7_DATE                DATE, \
    RISHI7_GK                   INT           NOT NULL, \
    KARIIRE_ZAN7_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN7_DATE       DATE, \
    E_RISOKU_KEISAN7_DATE       DATE, \
    RISOKU_KEISAN7_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI8_DATE                DATE, \
    RISHI8_GK                   INT           NOT NULL, \
    KARIIRE_ZAN8_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN8_DATE       DATE, \
    E_RISOKU_KEISAN8_DATE       DATE, \
    RISOKU_KEISAN8_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI9_DATE                DATE, \
    RISHI9_GK                   INT           NOT NULL, \
    KARIIRE_ZAN9_GK             INT           NOT NULL, \
    S_RISOKU_KEISAN9_DATE       DATE, \
    E_RISOKU_KEISAN9_DATE       DATE, \
    RISOKU_KEISAN9_NISUU        VARCHAR(3)   NOT NULL, \
    HENSAI10_DATE               DATE, \
    RISHI10_GK                  INT           NOT NULL, \
    KARIIRE_ZAN10_GK            INT           NOT NULL, \
    S_RISOKU_KEISAN10_DATE      DATE, \
    E_RISOKU_KEISAN10_DATE      DATE, \
    RISOKU_KEISAN10_NISUU       VARCHAR(3)   NOT NULL, \
    HENSAI11_DATE               DATE, \
    RISHI11_GK                  INT           NOT NULL, \
    KARIIRE_ZAN11_GK            INT           NOT NULL, \
    S_RISOKU_KEISAN11_DATE      DATE, \
    E_RISOKU_KEISAN11_DATE      DATE, \
    RISOKU_KEISAN11_NISUU       VARCHAR(3)   NOT NULL, \
    HENSAI12_DATE               DATE, \
    RISHI12_GK                  INT           NOT NULL, \
    KARIIRE_ZAN12_GK            INT           NOT NULL, \
    S_RISOKU_KEISAN12_DATE      DATE, \
    E_RISOKU_KEISAN12_DATE      DATE, \
    RISOKU_KEISAN12_NISUU       VARCHAR(3)   NOT NULL, \
    RISHI_YEAR_TOTAL_GK         INT, \
    SHUUGAKU_NO                 VARCHAR(7),             \
    SHINSEI_YEAR                VARCHAR(4),             \
    SHORI_KEKKACD               VARCHAR(1),             \
    ERROR_SHUUSEI_DIV           VARCHAR(1),             \
    SHUUGAKU_NO_D010            VARCHAR(7),             \
    SHINSEI_YEAR_D010           VARCHAR(4),             \
    SHORI_KEKKACD_D010          VARCHAR(1),             \
    ERROR_SHUUSEI_DIV_D010      VARCHAR(1),             \
    SHUUGAKU_NO_D020            VARCHAR(7),             \
    SHINSEI_YEAR_D020           VARCHAR(4),             \
    SHORI_KEKKACD_D020          VARCHAR(1),             \
    ERROR_SHUUSEI_DIV_D020      VARCHAR(1),             \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE RISHI_KEKKA_DAT ADD CONSTRAINT PK_RISHI_KEK_DAT PRIMARY KEY (SAKUSEI_DATE, BANKCD, SHORI_NO, SHORI_EDABAN)
