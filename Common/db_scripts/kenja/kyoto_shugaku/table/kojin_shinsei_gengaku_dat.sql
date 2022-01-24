-- $Id: kojin_shinsei_gengaku_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_SHINSEI_GENGAKU_DAT
CREATE TABLE KOJIN_SHINSEI_GENGAKU_DAT( \
    SHUUGAKU_NO              VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    GENGAKU_UKE_YEAR         VARCHAR(4), \
    GENGAKU_UKE_NO           VARCHAR(4), \
    GENGAKU_UKE_EDABAN       VARCHAR(3), \
    HENKOUGO_TAIYOGK         INT           NOT NULL, \
    HENKOUMAE_TAIYOGK        INT           NOT NULL, \
    S_KEIKAKU_HENKOU_YM      VARCHAR(7), \
    E_KEIKAKU_HENKOU_YM      VARCHAR(7), \
    KEIKAKU_UPDATE_FLG       VARCHAR(1), \
    KETTEI_DATE              DATE, \
    KETTEI_FLG               VARCHAR(1), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_SHINSEI_GENGAKU_DAT ADD CONSTRAINT PK_K_SHIN_G_DAT PRIMARY KEY (SHUUGAKU_NO, SHINSEI_YEAR)