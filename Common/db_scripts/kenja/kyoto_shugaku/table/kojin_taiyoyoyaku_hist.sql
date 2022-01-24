-- $Id: kojin_taiyoyoyaku_hist.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_TAIYOYOYAKU_HIST_DAT
CREATE TABLE KOJIN_TAIYOYOYAKU_HIST_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SHIKIN_SHOUSAI_DIV       VARCHAR(2)    NOT NULL, \
    UKE_YEAR                 VARCHAR(4), \
    UKE_NO                   VARCHAR(4), \
    UKE_EDABAN               VARCHAR(3), \
    YOYAKU_SHINSEI_DATE      DATE          NOT NULL, \
    SHUUGAKU_NO              VARCHAR(7), \
    NENREI                   SMALLINT, \
    KIKON_FLG                VARCHAR(1), \
    J_SCHOOL_CD              VARCHAR(7), \
    J_GRAD_DIV               VARCHAR(1), \
    J_GRAD_YM                VARCHAR(7), \
    KIBOU_H_SCHOOL_DIV       VARCHAR(1), \
    YOYAKU_KIBOU_GK          INT, \
    S_YOYAKU_KIBOU_YM        VARCHAR(7), \
    E_YOYAKU_KIBOU_YM        VARCHAR(7), \
    SHITAKUKIN_KIBOU_FLG     VARCHAR(1), \
    SHINSEI_DIV              VARCHAR(1)    NOT NULL, \
    SETAI_SEQ                SMALLINT, \
    SHORI_JYOUKYOU           VARCHAR(2), \
    RENTAI_CD                VARCHAR(7), \
    SHINKEN1_CD              VARCHAR(7), \
    SHINKEN2_CD              VARCHAR(7), \
    SHUTARU_CD               VARCHAR(7), \
    SHINSEI_KANRYOU_FLG      VARCHAR(1), \
    SHINSEI_CANCEL_FLG       VARCHAR(1), \
    SHINSEI_CANCEL_DATE      DATE, \
    KETTEI_DATE              DATE, \
    KETTEI_FLG               VARCHAR(1), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_TAIYOYOYAKU_HIST_DAT ADD CONSTRAINT PK_K_TYOYAK_H_DAT PRIMARY KEY (KOJIN_NO)