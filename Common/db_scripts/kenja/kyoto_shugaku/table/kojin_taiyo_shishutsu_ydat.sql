-- $Id: kojin_taiyo_shishutsu_ydat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_TAIYO_SHISHUTSU_YDAT
CREATE TABLE KOJIN_TAIYO_SHISHUTSU_YDAT( \
    SHUUGAKU_NO                         VARCHAR(7) NOT NULL, \
    SHIKIN_SHUBETSU                     VARCHAR(1) NOT NULL, \
    SHINSEI_YEAR                        VARCHAR(4) NOT NULL, \
    SHISHUTSU_TOTALNAI_KOKUKO_GK        INT, \
    SHISHUTSU_TOTALNAI_TAN_GK           INT, \
    SHISHUTSU_TOTALNAI_KOUHU_GK         INT, \
    REGISTERCD                          VARCHAR(8), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_TAIYO_SHISHUTSU_YDAT ADD CONSTRAINT PK_K_T_SHI_YDAT PRIMARY KEY (SHUUGAKU_NO, SHIKIN_SHUBETSU, SHINSEI_YEAR)