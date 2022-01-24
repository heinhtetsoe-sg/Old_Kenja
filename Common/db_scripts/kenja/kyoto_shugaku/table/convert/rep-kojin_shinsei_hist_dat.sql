-- $Id: rep-kojin_shinsei_hist_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_SHINSEI_HIST_DAT_OLD
RENAME TABLE KOJIN_SHINSEI_HIST_DAT TO KOJIN_SHINSEI_HIST_DAT_OLD
CREATE TABLE KOJIN_SHINSEI_HIST_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SHIKIN_SHOUSAI_DIV       VARCHAR(2)    NOT NULL, \
    ISSUEDATE                DATE          NOT NULL, \
    UKE_YEAR                 VARCHAR(4), \
    UKE_NO                   VARCHAR(4), \
    UKE_EDABAN               VARCHAR(3), \
    SHINSEI_DATE             DATE          NOT NULL, \
    SHUUGAKU_NO              VARCHAR(7), \
    NENREI                   SMALLINT, \
    YOYAKU_KIBOU_GK          INT, \
    S_YOYAKU_KIBOU_YM        VARCHAR(7), \
    E_YOYAKU_KIBOU_YM        VARCHAR(7), \
    S_TAIYO_YM               VARCHAR(7), \
    E_TAIYO_YM               VARCHAR(7), \
    SHINSEI_DIV              VARCHAR(1)    NOT NULL, \
    SETAI_SEQ                SMALLINT, \
    SHORI_JYOUKYOU           VARCHAR(2), \
    KEIZOKU_KAISUU           VARCHAR(1), \
    HEIKYUU_SHOUGAKU_STATUS1 VARCHAR(1), \
    HEIKYUU_SHOUGAKU_REMARK1 VARCHAR(120), \
    HEIKYUU_SHOUGAKU_STATUS2 VARCHAR(1), \
    HEIKYUU_SHOUGAKU_GYOUMUCD2 VARCHAR(2), \
    HEIKYUU_SHOUGAKU_REMARK2 VARCHAR(120), \
    HEIKYUU_SHOUGAKU_STATUS3 VARCHAR(1), \
    HEIKYUU_SHOUGAKU_GYOUMUCD3 VARCHAR(2), \
    HEIKYUU_SHOUGAKU_REMARK3 VARCHAR(120), \
    SHITAKU_CANCEL_CHOKU_FLG VARCHAR(1), \
    SHITAKU_CANCEL_RI_FLG    VARCHAR(1), \
    H_SCHOOL_CD              VARCHAR(7), \
    KATEI                    VARCHAR(2), \
    GRADE                    VARCHAR(2), \
    ENT_DATE                 DATE, \
    H_GRAD_YM                VARCHAR(7), \
    SHITAKUKIN_TAIYO_DIV     VARCHAR(1), \
    HEIKYUU_SHITAKU_STATUS1  VARCHAR(1), \
    HEIKYUU_SHITAKU_REMARK1  VARCHAR(120), \
    HEIKYUU_SHITAKU_STATUS2  VARCHAR(1), \
    HEIKYUU_SHITAKU_GYOUMUCD2 VARCHAR(2), \
    HEIKYUU_SHITAKU_REMARK2  VARCHAR(120), \
    HEIKYUU_SHITAKU_STATUS3  VARCHAR(1), \
    HEIKYUU_SHITAKU_GYOUMUCD3 VARCHAR(2), \
    HEIKYUU_SHITAKU_REMARK3  VARCHAR(120), \
    YUUSHI_FAIL              VARCHAR(1), \
    YUUSHI_FAIL_DIV          VARCHAR(1), \
    YUUSHI_FAIL_REMARK       VARCHAR(150), \
    BANK_CD                  VARCHAR(6), \
    YUUSHI_COURSE_DIV        VARCHAR(1), \
    RENTAI_CD                VARCHAR(7), \
    SHINKEN1_CD              VARCHAR(7), \
    SHINKEN2_CD              VARCHAR(7), \
    SHUTARU_CD               VARCHAR(7), \
    SHINSEI_KANRYOU_FLG      VARCHAR(1), \
    SHINSEI_CANCEL_FLG       VARCHAR(1), \
    KETTEI_DATE              DATE, \
    KETTEI_FLG               VARCHAR(1), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO KOJIN_SHINSEI_HIST_DAT \
    SELECT \
        KOJIN_NO                 , \
        SHINSEI_YEAR             , \
        SHIKIN_SHOUSAI_DIV       , \
        ISSUEDATE                , \
        UKE_YEAR                 , \
        UKE_NO                   , \
        UKE_EDABAN               , \
        SHINSEI_DATE             , \
        SHUUGAKU_NO              , \
        NENREI                   , \
        YOYAKU_KIBOU_GK          , \
        S_YOYAKU_KIBOU_YM        , \
        E_YOYAKU_KIBOU_YM        , \
        S_TAIYO_YM               , \
        E_TAIYO_YM               , \
        SHINSEI_DIV              , \
        01, \
        CAST(NULL AS VARCHAR(1)) , \
        KEIZOKU_KAISUU           , \
        HEIKYUU_SHOUGAKU_STATUS1 , \
        HEIKYUU_SHOUGAKU_REMARK1 , \
        HEIKYUU_SHOUGAKU_STATUS2 , \
        HEIKYUU_SHOUGAKU_GYOUMUCD2, \
        HEIKYUU_SHOUGAKU_REMARK2 , \
        HEIKYUU_SHOUGAKU_STATUS3 , \
        HEIKYUU_SHOUGAKU_GYOUMUCD3, \
        HEIKYUU_SHOUGAKU_REMARK3 , \
        SHITAKU_CANCEL_CHOKU_FLG , \
        SHITAKU_CANCEL_RI_FLG    , \
        H_SCHOOL_CD              , \
        KATEI                    , \
        GRADE                    , \
        ENT_DATE                 , \
        H_GRAD_YM                , \
        SHITAKUKIN_TAIYO_DIV     , \
        HEIKYUU_SHITAKU_STATUS1  , \
        HEIKYUU_SHITAKU_REMARK1  , \
        HEIKYUU_SHITAKU_STATUS2  , \
        HEIKYUU_SHITAKU_GYOUMUCD2, \
        HEIKYUU_SHITAKU_REMARK2  , \
        HEIKYUU_SHITAKU_STATUS3  , \
        HEIKYUU_SHITAKU_GYOUMUCD3, \
        HEIKYUU_SHITAKU_REMARK3  , \
        YUUSHI_FAIL              , \
        YUUSHI_FAIL_DIV          , \
        YUUSHI_FAIL_REMARK       , \
        BANK_CD                  , \
        YUUSHI_COURSE_DIV        , \
        SUBSTR(RENTAI_CD, 1, 7)  , \
        SUBSTR(SHINKEN1_CD, 1, 7), \
        SUBSTR(SHINKEN2_CD, 1, 7), \
        SUBSTR(SHUTARU_CD, 1, 7), \
        SHINSEI_KANRYOU_FLG      , \
        SHINSEI_CANCEL_FLG       , \
        KETTEI_DATE              , \
        KETTEI_FLG               , \
        REGISTERCD, \
        UPDATED \
    FROM \
        KOJIN_SHINSEI_HIST_DAT_OLD

ALTER TABLE KOJIN_SHINSEI_HIST_DAT ADD CONSTRAINT PK_K_SHIN_H_DAT PRIMARY KEY (KOJIN_NO, SHINSEI_YEAR, SHIKIN_SHOUSAI_DIV, ISSUEDATE)
