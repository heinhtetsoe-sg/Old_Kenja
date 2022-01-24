-- $Id: kojin_idou_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_IDOU_DAT
CREATE TABLE KOJIN_IDOU_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    IDOU_DIV                 VARCHAR(2)    NOT NULL, \
    SHINSEI_DATE             DATE          NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SHUUGAKU_NO              VARCHAR(7)    NOT NULL, \
    SEQ                      SMALLINT      NOT NULL, \
    IDOU_UKE_YEAR            VARCHAR(4), \
    IDOU_UKE_NO              VARCHAR(4), \
    IDOU_UKE_EDABAN          VARCHAR(3), \
    IDOU_DATE                DATE, \
    S_IDOU_DATE              DATE, \
    E_IDOU_DATE              DATE, \
    S_SAIKAI_YOTEI_YM        VARCHAR(7), \
    E_SAIKAI_YOTEI_YM        VARCHAR(7), \
    TENGAKUSAKI_HU_DIV       VARCHAR(1), \
    SCHOOL_CD                VARCHAR(7), \
    SCHOOL_CD_OLD            VARCHAR(7), \
    KATEI                    VARCHAR(2), \
    KATEI_OLD                VARCHAR(2), \
    TSUUGAKU_FLG             VARCHAR(1), \
    TSUUGAKU_DIV             VARCHAR(1), \
    TSUUGAKU_DIV_OLD         VARCHAR(1), \
    S_STOP_YOTEI_YM          VARCHAR(7), \
    E_STOP_YOTEI_YM          VARCHAR(7), \
    CHANGE_GK                INT, \
    S_CHANGE_GK_YM           VARCHAR(7), \
    E_CHANGE_GK_YM           VARCHAR(7), \
    REMARK                   VARCHAR(2400), \
    S_KETTEI_YM              VARCHAR(7), \
    E_KETTEI_YM              VARCHAR(7), \
    STOP_KETTEI_DATE         DATE, \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_IDOU_DAT ADD CONSTRAINT PK_K_IDOU_DAT PRIMARY KEY (KOJIN_NO, IDOU_DIV, SHINSEI_DATE, SHINSEI_YEAR, SHUUGAKU_NO, SEQ)