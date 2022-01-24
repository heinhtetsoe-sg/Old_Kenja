-- $Id: kyuhu_sonota_csv_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KYUHU_SONOTA_CSV_DAT
CREATE TABLE KYUHU_SONOTA_CSV_DAT( \
    SONOTA_DIV              VARCHAR(1)    NOT NULL, \
    SHINSEI_YEAR            VARCHAR(4)    NOT NULL, \
    SEIRI_NO                VARCHAR(15)   NOT NULL, \
    FAMILY_NAME             VARCHAR(60), \
    FIRST_NAME              VARCHAR(60), \
    FAMILY_NAME_KANA        VARCHAR(120)  NOT NULL, \
    FIRST_NAME_KANA         VARCHAR(120)  NOT NULL, \
    BIRTHDAY                DATE NOT NULL, \
    SCHOOL_NAME             VARCHAR(75), \
    ZIPCD                   VARCHAR(8), \
    ADDR1                   VARCHAR(150), \
    ADDR2                   VARCHAR(150), \
    ADDR3                   VARCHAR(150), \
    HOGO_FAMILY_NAME        VARCHAR(60), \
    HOGO_FIRST_NAME         VARCHAR(60), \
    HOGO_ZIPCD              VARCHAR(8), \
    ADDR_CODE               VARCHAR(5), \
    HOGO_ADDR1              VARCHAR(150), \
    HOGO_ADDR2              VARCHAR(150), \
    KOJIN_NO                VARCHAR(7), \
    SHUUGAKU_NO             VARCHAR(7), \
    SONOTA_KYUHU_FLG        VARCHAR(1), \
    SONOTA_KYUHU_GK         INT, \
    KAKUTEI_FLG             VARCHAR(1), \
    CAPTURE_DATE            date, \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KYUHU_SONOTA_CSV_DAT ADD CONSTRAINT PK_KYU_SONOTA_CSV PRIMARY KEY (SONOTA_DIV, SHINSEI_YEAR, SEIRI_NO)
