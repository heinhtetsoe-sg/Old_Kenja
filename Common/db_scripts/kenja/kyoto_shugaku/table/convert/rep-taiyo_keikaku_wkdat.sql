-- $Id: rep-taiyo_keikaku_wkdat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE TAIYO_KEIKAKU_WKDAT_OLD
RENAME TABLE TAIYO_KEIKAKU_WKDAT TO TAIYO_KEIKAKU_WKDAT_OLD
CREATE TABLE TAIYO_KEIKAKU_WKDAT( \
    SHUUGAKU_NO              VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    YEAR                     VARCHAR(4)    NOT NULL, \
    MONTH                    VARCHAR(2)    NOT NULL, \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    SHIKIN_SHUBETSU          VARCHAR(1)    NOT NULL, \
    FURIKOMI_YOTEI_DATE      DATE, \
    FURIKOMI_DATE            DATE, \
    SHIHARAI_PLAN_GK         INT           NOT NULL, \
    SHISHUTSU_YOTEI_GK       INT, \
    SHISHUTSU_GK             INT, \
    HENNOU_YOTEI_GK          INT, \
    GENGAKU_KARI_TEISHI_FLG  VARCHAR(1), \
    KARI_TEISHI_FLG          VARCHAR(1), \
    TEISHI_FLG               VARCHAR(1), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO TAIYO_KEIKAKU_WKDAT \
    SELECT \
        SHUUGAKU_NO        , \
        SHINSEI_YEAR       , \
        YEAR               , \
        MONTH              , \
        KOJIN_NO           , \
        SHIKIN_SHUBETSU    , \
        FURIKOMI_YOTEI_DATE, \
        FURIKOMI_DATE      , \
        SHIHARAI_PLAN_GK   , \
        SHISHUTSU_YOTEI_GK , \
        SHISHUTSU_GK       , \
        HENNOU_YOTEI_GK    , \
        CAST(NULL AS VARCHAR(1)) AS GENGAKU_KARI_TEISHI_FLG, \
        KARI_TEISHI_FLG, \
        TEISHI_FLG         , \
        REGISTERCD, \
        UPDATED \
    FROM \
        TAIYO_KEIKAKU_WKDAT_OLD

ALTER TABLE TAIYO_KEIKAKU_WKDAT ADD CONSTRAINT PK_TAIYO_KE_WDAT PRIMARY KEY (SHUUGAKU_NO, SHINSEI_YEAR, YEAR, MONTH)