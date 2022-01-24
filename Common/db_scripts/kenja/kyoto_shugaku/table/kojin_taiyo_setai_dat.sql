-- $Id: kojin_taiyo_setai_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_TAIYO_SETAI_DAT
CREATE TABLE KOJIN_TAIYO_SETAI_DAT( \
    KOJIN_NO                 VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SETAI_SEQ                SMALLINT       NOT NULL, \
    SEQ                      SMALLINT       NOT NULL, \
    FAMILY_NAME              VARCHAR(60)    NOT NULL, \
    FIRST_NAME               VARCHAR(60)    NOT NULL, \
    FIRST_NAME_KANA          VARCHAR(120)   , \
    FAMILY_NAME_KANA         VARCHAR(120)   , \
    TSUZUKIGARA_CD           VARCHAR(2)     NOT NULL, \
    NENREI                   SMALLINT, \
    KYOUDAI_KOJIN_NO         VARCHAR(7), \
    SHOTOKU_CD               VARCHAR(1), \
    SHOTOKU_GK               INT, \
    KOUJO_FUBO_IGAI          INT, \
    KOUJO_HOKEN              INT, \
    KOUJO_SHOTOKU            INT, \
    NINTEI_GK                INT, \
    SHUTARU_FLG              VARCHAR(1), \
    SETAINUSHI_FLG           VARCHAR(1), \
    REMARK                   VARCHAR(2400), \
    REASON                   VARCHAR(150), \
    SHIKIN_DIV               VARCHAR(1)     NOT NULL, \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_TAIYO_SETAI_DAT ADD CONSTRAINT PK_K_TAIYO_SE_DAT PRIMARY KEY (KOJIN_NO, SHINSEI_YEAR, SETAI_SEQ, SEQ)