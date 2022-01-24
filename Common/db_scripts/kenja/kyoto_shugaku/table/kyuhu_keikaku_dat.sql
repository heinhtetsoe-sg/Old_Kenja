-- $Id: kyuhu_keikaku_dat.sql 74686 2020-06-03 11:34:47Z yamashiro $

DROP TABLE KYUHU_KEIKAKU_DAT
CREATE TABLE KYUHU_KEIKAKU_DAT( \
    SHUUGAKU_NO              VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR             VARCHAR(4)    NOT NULL, \
    SEQ                      smallint      NOT NULL, \
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
    KARI_TEISHI_FLG          VARCHAR(1), \
    TEISHI_FLG               VARCHAR(1), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KYUHU_KEIKAKU_DAT ADD CONSTRAINT PK_KYUHU_KE_DAT PRIMARY KEY (SHUUGAKU_NO, SHINSEI_YEAR, SEQ, YEAR, MONTH)