-- $Id: rep-kyuhu_keikaku_dat.sql 74686 2020-06-03 11:34:47Z yamashiro $

drop table KYUHU_KEIKAKU_DAT_OLD

create table KYUHU_KEIKAKU_DAT_OLD like KYUHU_KEIKAKU_DAT

insert into KYUHU_KEIKAKU_DAT_OLD select * from KYUHU_KEIKAKU_DAT

drop table KYUHU_KEIKAKU_DAT

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

insert into KYUHU_KEIKAKU_DAT \
select \
    SHUUGAKU_NO, \
    SHINSEI_YEAR, \
    1 as SEQ, \
    YEAR, \
    MONTH, \
    KOJIN_NO, \
    SHIKIN_SHUBETSU, \
    FURIKOMI_YOTEI_DATE, \
    FURIKOMI_DATE, \
    SHIHARAI_PLAN_GK, \
    SHISHUTSU_YOTEI_GK, \
    SHISHUTSU_GK, \
    HENNOU_YOTEI_GK, \
    KARI_TEISHI_FLG, \
    TEISHI_FLG, \
    REGISTERCD, \
    UPDATED \
from \
KYUHU_KEIKAKU_DAT_OLD
