-- $Id: kojin_taiyo_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_TAIYO_DAT
CREATE TABLE KOJIN_TAIYO_DAT( \
    SHUUGAKU_NO                         VARCHAR(7) NOT NULL, \
    SHIKIN_SHUBETSU                     VARCHAR(1) NOT NULL, \
    KOJIN_NO                            VARCHAR(7) NOT NULL, \
    YOYAKU_SHUUGAKU_NO                  VARCHAR(7), \
    SOTUGYOUYOTEI_DATE                  DATE,   \
    S_TAIYOKIBOU_DATE                   DATE,   \
    E_TAIYOKIBOU_DATE                   DATE,   \
    TAIYO_YOTEITOTAL_MONTHS             SMALLINT, \
    SHISHUTSU_TOTAL_MONTHS              SMALLINT, \
    SHISHUTSU_TOTAL_GK                  INT, \
    SHISHUTSU_TOTALNAI_KOKUKO_GK        INT, \
    SHISHUTSU_TOTALNAI_TAN_GK           INT, \
    SHISHUTSU_TOTALNAI_KOUHU_GK         INT, \
    SHISHUTSU_SHORI_TOTAL_GK            INT, \
    HENNOU_YOTEITOTAL_GK                INT, \
    HENNOU_TOTAL_GK                     INT, \
    U_HENNOU_TOTAL_GK                   INT, \
    U_ZATSUNYU_TOTAL_GK                 INT, \
    HENKAN_CHOUTEI_GK                   INT, \
    HENKAN_CHOUTEI_KAISUU               SMALLINT, \
    HENNOU_CHOUTEI_GK                   INT, \
    HENNOU_CHOUTEI_KAISUU               SMALLINT, \
    SHUUNOU_TOTAL_GK                    INT, \
    SHUUNOU_TOTALNAI_KOKUKO_GK          INT, \
    SHUUNOU_TOTALNAI_TAN_GK             INT, \
    SHUUNOU_TOTALNAI_KOUHU_GK           INT, \
    MENJYO_TOTAL_GK                     INT, \
    MENJYO_TOTALNAI_KOKUKO_GK           INT, \
    MENJYO_TOTALNAI_TAN_GK              INT, \
    MENJYO_TOTALNAI_KOUHU_GK            INT, \
    FUNOU_TOTAL_GK                      INT, \
    FUNOU_TOTALNAI_KOKUKO_GK            INT, \
    FUNOU_TOTALNAI_TAN_GK               INT, \
    FUNOU_TOTALNAI_KOUHU_GK             INT, \
    HENKAN_UKE_DATE                     DATE, \
    HENKAN_GK                           INT, \
    HENKAN_KAISUU                       SMALLINT, \
    HENKAN_HOUHOU_CD                    VARCHAR(1), \
    HENKAN_KIKAN                        SMALLINT, \
    S_HENKAN_YM                         VARCHAR(7),   \
    E_HENKAN_YM                         VARCHAR(7),   \
    HENKAN_SHIHARAIKUBUN_CD             VARCHAR(1),   \
    HENKAN_SHIHARAI_CD                  VARCHAR(1),   \
    HENKAN_HOSHOUNIN_CD                 VARCHAR(7),   \
    HENKAN_SHIHARAININ_CD               VARCHAR(7),   \
    HENKAN_REMARK                       VARCHAR(200),   \
    MENJYO_UKE_YEAR                     VARCHAR(4),   \
    MENJYO_UKE_NO                       VARCHAR(4),   \
    MENJYO_UKE_EDABAN                   VARCHAR(3),   \
    MENJYO_SHINSEI_DATE                 DATE, \
    MENJYO_JIYUU_CD                     VARCHAR(1),   \
    MENJYO_JIYUU_REMARK                 VARCHAR(200),   \
    MENJYO_SHINSEI_KEKKA_CD             VARCHAR(1),   \
    MENJYO_SHOUNIN_DATE                 DATE, \
    HENNOU_UKE_DATE                     DATE, \
    HENNOU_GK                           INT, \
    HENNOU_KAISUU                       SMALLINT, \
    HENNOU_HOUHOU_CD                    VARCHAR(1), \
    HENNOU_KIKAN                        SMALLINT, \
    S_HENNOU_YM                         VARCHAR(7),   \
    E_HENNOU_YM                         VARCHAR(7),   \
    HENNOU_SHIHARAI_KUBUN_CD            VARCHAR(1),   \
    HENNOU_SHIHARAI_CD                  VARCHAR(1),   \
    HENNOU_HOSHOUNIN_CD                 VARCHAR(7),   \
    HENNOU_SHIHARAININ_CD               VARCHAR(7),   \
    HENNOU_REMARK                       VARCHAR(200),   \
    SHUGAKU_FLG                         VARCHAR(1),   \
    HENNOU_STATUS_FLG                   VARCHAR(1),   \
    SOUGOU_STATUS_FLG                   VARCHAR(1),   \
    HENKAN_KEISAN_CD                    VARCHAR(1),   \
    HENKAN_1KAI_GK                      INT, \
    REGISTERCD                          VARCHAR(8), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_TAIYO_DAT ADD CONSTRAINT PK_KOJIN_TAIYO_DAT PRIMARY KEY (SHUUGAKU_NO, SHIKIN_SHUBETSU)
