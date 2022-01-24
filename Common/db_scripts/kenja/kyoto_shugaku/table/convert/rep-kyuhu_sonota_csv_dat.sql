-- $Id: rep-kyuhu_sonota_csv_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KYUHU_SONOTA_CSV_DAT_OLD
RENAME TABLE KYUHU_SONOTA_CSV_DAT TO KYUHU_SONOTA_CSV_DAT_OLD
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

INSERT INTO KYUHU_SONOTA_CSV_DAT \
    SELECT \
        SONOTA_DIV          , \
        SHINSEI_YEAR        , \
        SEIRI_NO            , \
        FAMILY_NAME         , \
        FIRST_NAME          , \
        FAMILY_NAME_KANA    , \
        FIRST_NAME_KANA     , \
        BIRTHDAY            , \
        SCHOOL_NAME         , \
        ZIPCD               , \
        ADDR1               , \
        ADDR2               , \
        ADDR3               , \
        HOGO_FAMILY_NAME    , \
        HOGO_FIRST_NAME     , \
        HOGO_ZIPCD          , \
        ADDR_CODE           , \
        HOGO_ADDR1          , \
        HOGO_ADDR2          , \
        KOJIN_NO            , \
        SHUUGAKU_NO         , \
        SONOTA_KYUHU_FLG    , \
        SONOTA_KYUHU_GK     , \
        KAKUTEI_FLG         , \
        CAST(NULL AS date) AS CAPTURE_DATE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        KYUHU_SONOTA_CSV_DAT_OLD

ALTER TABLE KYUHU_SONOTA_CSV_DAT ADD CONSTRAINT PK_KYU_SONOTA_CSV PRIMARY KEY (SONOTA_DIV, SHINSEI_YEAR, SEIRI_NO)
