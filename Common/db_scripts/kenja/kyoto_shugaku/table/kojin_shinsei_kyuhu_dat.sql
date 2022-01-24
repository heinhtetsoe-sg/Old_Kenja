-- $Id: kojin_shinsei_kyuhu_dat.sql 74605 2020-05-29 09:33:29Z yamashiro $

DROP TABLE KOJIN_SHINSEI_KYUHU_DAT
CREATE TABLE KOJIN_SHINSEI_KYUHU_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SEQ                      smallint      NOT NULL, \
    SHINSEI_DATE             DATE          NOT NULL, \
    KAKEI_KYUHEN_FLG         VARCHAR(1), \            
    KAKEI_KYUHEN_DATE        DATE, \            
    UKE_YEAR                 VARCHAR(4), \            
    UKE_NO                   VARCHAR(4), \            
    UKE_EDABAN               VARCHAR(3), \            
    SHUUGAKU_NO              VARCHAR(7), \            
    SHINSEI_DIV              VARCHAR(1)    NOT NULL, \
    SETAI_SEQ                SMALLINT, \              
    SHORI_JYOUKYOU           VARCHAR(2), \            
    HOGOSHA_CD               VARCHAR(7), \            
    HOGOSHA2_CD              VARCHAR(7), \            
    KYUHU_KAISUU             SMALLINT, \              
    H_SCHOOL_CD              VARCHAR(7)    NOT NULL, \
    KOUSHI_DIV               VARCHAR(4)    NOT NULL, \
    KATEI                    VARCHAR(2)    NOT NULL, \
    KATEI_DIV                VARCHAR(1)    NOT NULL, \
    GRADE                    VARCHAR(2), \            
    HR_CLASS                 VARCHAR(3), \            
    ATTENDNO                 VARCHAR(3), \            
    SHOTOKUWARI_DIV          VARCHAR(1)    NOT NULL, \
    SHOTOKUWARI_GK           INT, \
    SHOTOKUWARI_GK_CHECK_FLG VARCHAR(1), \            
    KYOUDAI_BIRTHDAY         DATE, \
    KYOUDAI_TSUZUKIGARA_CD   VARCHAR(2), \
    KYOUDAI_FAMILY_NAME      VARCHAR(60), \
    KYOUDAI_FIRST_NAME       VARCHAR(60), \
    KYOUDAI_FAMILY_NAME_KANA VARCHAR(120), \
    KYOUDAI_FIRST_NAME_KANA  VARCHAR(120), \
    HEIKYUU_SHOUGAKU_FLG1    VARCHAR(1), \
    HEIKYUU_SHOUGAKU_FLG2    VARCHAR(1), \
    KANRYOU_FLG              VARCHAR(1), \
    KYUHU_YOTEI_GK           INT, \
    KETTEI_CANCEL_UKE_YEAR   VARCHAR(4), \
    KETTEI_CANCEL_UKE_NO     VARCHAR(4), \
    KETTEI_CANCEL_UKE_EDABAN VARCHAR(3), \
    KETTEI_CANCEL_DATE       DATE, \
    CANCEL_FLG               VARCHAR(1), \
    KETTEI_DATE              DATE, \
    KETTEI_FLG               VARCHAR(1), \
    REMARK                   VARCHAR(2400), \
    REGISTERCD               VARCHAR(8), \            
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_SHINSEI_KYUHU_DAT ADD CONSTRAINT PK_K_SHIN_K_DAT PRIMARY KEY (KOJIN_NO, SHINSEI_YEAR, SEQ)

