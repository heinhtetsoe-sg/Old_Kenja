-- $Id: rep-kojin_taiyo_shinsa_kekka_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_TAIYO_SHINSA_KEKKA_DAT_OLD
RENAME TABLE KOJIN_TAIYO_SHINSA_KEKKA_DAT TO KOJIN_TAIYO_SHINSA_KEKKA_DAT_OLD
CREATE TABLE KOJIN_TAIYO_SHINSA_KEKKA_DAT( \
    KOJIN_NO                    VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR                VARCHAR(4)    NOT NULL, \
    SHIKIN_SHOUSAI_DIV          VARCHAR(2)    NOT NULL, \
    ISSUEDATE                   DATE          NOT NULL, \
    KEKKA_DIV                   VARCHAR(1)    NOT NULL, \
    SETAI_SEQ                   SMALLINT      NOT NULL, \
    SHIKIN_DIV                  VARCHAR(1), \
    SHINSA_KEKKA                VARCHAR(1), \
    HUYOU_COUNT                 SMALLINT, \
    HUYOU_16_UNDER              SMALLINT, \
    HUYOU_16_ANDOVER_19_UNDER   SMALLINT, \
    SHOTOKU_WARI_GK             INT, \
    SSHOTOKU_KIJUN_SETAI_COUNT  SMALLINT, \
    SSHOTOKU_KIJUN_GK           INT, \
    SYEAR_SALARY_GK             INT, \
    SYEAR_NOTSALARY_GK          INT, \
    SYEAR_KOUJYO_GK             INT, \
    SYEAR_TOKU_KOUJYO_GK        INT, \
    SYEAR_SHOTOKU_NINTEI_GK     INT, \
    SNINTEI_KEKKA_FLG           VARCHAR(1), \
    SETAI_STATUS                VARCHAR(2), \
    SETAI_COUNT                 SMALLINT, \
    BOSHI_FUSHI_SETAI_FLG       VARCHAR(1), \
    JUNIOR_UNDER_FLG            VARCHAR(1), \
    SHOUGAISHA_FLG              VARCHAR(1), \
    SHOUGAISHA_COUNT            SMALLINT, \
    KAKUNIN_REPORT              VARCHAR(75), \
    ROUNENSHA_FLG               VARCHAR(1), \
    ROUNENSHA_COUNT             SMALLINT, \
    LONG_RYOUYOUSHA_GK          INT, \
    AREA_CHOUSA_FLG             VARCHAR(1), \
    KYUUCHI                     VARCHAR(1), \
    AREA_CHOUSA_COUNT           SMALLINT, \
    SHOTOKU_KIJYUN_GK           INT, \
    NINTEI_SOUSHUNYU_GK         INT, \
    TAIYO_HANTEI_KEKKA          VARCHAR(1), \
    TAIYO_FAIL_DIV              VARCHAR(1), \
    TAIYO_FAIL_REMARK           VARCHAR(150), \
    NINTEI_HANTEI_KEKKA         VARCHAR(1), \
    TBOSHI_FLG                      VARCHAR(1), \
    TSYOUGAKU_COUNT                 SMALLINT, \
    TCHUUGAKU_COUNT                 SMALLINT, \
    TKOUKOU_JI_COUNT                SMALLINT, \
    TKOUKOU_JIGAI_COUNT             SMALLINT, \
    TKOUKOU_SHI_JI_COUNT            SMALLINT, \
    TKOUKOU_SHI_JIGAI_COUNT         SMALLINT, \
    TSENMON_KO_JI_COUNT             SMALLINT, \
    TSENMON_KO_JIGAI_COUNT          SMALLINT, \
    TSENMON_SHI_JI_COUNT            SMALLINT, \
    TSENMON_SHI_JIGAI_COUNT         SMALLINT, \
    TDAIGAKU_KO_JI_COUNT            SMALLINT, \
    TDAIGAKU_KO_JIGAI_COUNT         SMALLINT, \
    TDAIGAKU_SHI_JI_COUNT           SMALLINT, \
    TDAIGAKU_SHI_JIGAI_COUNT        SMALLINT, \
    TSENSYU_KO_KO_JI_COUNT          SMALLINT, \
    TSENSYU_KO_KO_JIGAI_COUNT       SMALLINT, \
    TSENSYU_KO_SHI_JI_COUNT         SMALLINT, \
    TSENSYU_KO_SHI_JIGAI_COUNT      SMALLINT, \
    TSENSYU_SEN_KO_JI_COUNT         SMALLINT, \
    TSENSYU_SEN_KO_JIGAI_COUNT      SMALLINT, \
    TSENSYU_SEN_SHI_JI_COUNT        SMALLINT, \
    TSENSYU_SEN_SHI_JIGAI_COUNT     SMALLINT, \
    TSHOUGAI_COUNT                  SMALLINT, \
    TLONG_RYOUYOU_GK                INT, \
    TBEKKYO_GK                      INT, \
    TSAIGAI_GK                      INT, \
    TSONOTA_GK                      INT, \
    TSONOTA_REMARK                  VARCHAR(150), \
    REGISTERCD                      VARCHAR(8), \
    UPDATED                         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO KOJIN_TAIYO_SHINSA_KEKKA_DAT \
    SELECT \
        KOJIN_NO                    , \
        SHINSEI_YEAR                , \
        SHIKIN_SHOUSAI_DIV, \
        ISSUEDATE, \
        KEKKA_DIV                   , \
        SETAI_SEQ, \
        SHIKIN_DIV                  , \
        SHINSA_KEKKA                , \
        CAST(NULL AS SMALLINT)  AS HUYOU_COUNT, \
        CAST(NULL AS SMALLINT)  AS HUYOU_16_UNDER, \
        CAST(NULL AS SMALLINT)  AS HUYOU_16_ANDOVER_19_UNDER, \
        CAST(NULL AS INT)       AS SHOTOKU_WARI_GK, \
        SSHOTOKU_KIJUN_SETAI_COUNT  , \
        SSHOTOKU_KIJUN_GK           , \
        SYEAR_SALARY_GK             , \
        SYEAR_NOTSALARY_GK          , \
        SYEAR_KOUJYO_GK             , \
        SYEAR_TOKU_KOUJYO_GK        , \
        SYEAR_SHOTOKU_NINTEI_GK     , \
        SNINTEI_KEKKA_FLG           , \
        SETAI_STATUS                , \
        SETAI_COUNT                 , \
        BOSHI_FUSHI_SETAI_FLG       , \
        JUNIOR_UNDER_FLG            , \
        SHOUGAISHA_FLG              , \
        SHOUGAISHA_COUNT            , \
        KAKUNIN_REPORT              , \
        ROUNENSHA_FLG               , \
        ROUNENSHA_COUNT             , \
        LONG_RYOUYOUSHA_GK          , \
        AREA_CHOUSA_FLG             , \
        KYUUCHI                     , \
        AREA_CHOUSA_COUNT           , \
        SHOTOKU_KIJYUN_GK           , \
        NINTEI_SOUSHUNYU_GK         , \
        TAIYO_HANTEI_KEKKA          , \
        TAIYO_FAIL_DIV              , \
        TAIYO_FAIL_REMARK           , \
        NINTEI_HANTEI_KEKKA         , \
        TBOSHI_FLG                  , \
        TSYOUGAKU_COUNT             , \
        TCHUUGAKU_COUNT             , \
        TKOUKOU_JI_COUNT            , \
        TKOUKOU_JIGAI_COUNT         , \
        TKOUKOU_SHI_JI_COUNT        , \
        TKOUKOU_SHI_JIGAI_COUNT     , \
        TSENMON_KO_JI_COUNT         , \
        TSENMON_KO_JIGAI_COUNT      , \
        TSENMON_SHI_JI_COUNT        , \
        TSENMON_SHI_JIGAI_COUNT     , \
        TDAIGAKU_KO_JI_COUNT        , \
        TDAIGAKU_KO_JIGAI_COUNT     , \
        TDAIGAKU_SHI_JI_COUNT       , \
        TDAIGAKU_SHI_JIGAI_COUNT    , \
        TSENSYU_KO_KO_JI_COUNT      , \
        TSENSYU_KO_KO_JIGAI_COUNT   , \
        TSENSYU_KO_SHI_JI_COUNT     , \
        TSENSYU_KO_SHI_JIGAI_COUNT  , \
        TSENSYU_SEN_KO_JI_COUNT     , \
        TSENSYU_SEN_KO_JIGAI_COUNT  , \
        TSENSYU_SEN_SHI_JI_COUNT    , \
        TSENSYU_SEN_SHI_JIGAI_COUNT , \
        TSHOUGAI_COUNT              , \
        TLONG_RYOUYOU_GK            , \
        TBEKKYO_GK                  , \
        TSAIGAI_GK                  , \
        TSONOTA_GK                  , \
        TSONOTA_REMARK              , \
        REGISTERCD, \
        UPDATED \
    FROM \
        KOJIN_TAIYO_SHINSA_KEKKA_DAT_OLD

ALTER TABLE KOJIN_TAIYO_SHINSA_KEKKA_DAT ADD CONSTRAINT PK_K_T_SH_KEK_DAT PRIMARY KEY (KOJIN_NO, SHINSEI_YEAR, SHIKIN_SHOUSAI_DIV, ISSUEDATE, KEKKA_DIV)