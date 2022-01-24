-- kanji=����
-- $Id: levy_request_outgo_meisai_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����ٽлǤ����٥ǡ���

DROP TABLE LEVY_REQUEST_OUTGO_MEISAI_DAT

CREATE TABLE LEVY_REQUEST_OUTGO_MEISAI_DAT \
( \
        "YEAR"                  VARCHAR(4)  NOT NULL, \
        "OUTGO_L_CD"            VARCHAR(2)  NOT NULL, \
        "OUTGO_M_CD"            VARCHAR(2)  NOT NULL, \
        "REQUEST_NO"            VARCHAR(10) NOT NULL, \
        "OUTGO_S_CD"            VARCHAR(2)  NOT NULL, \
        "LINE_NO"               SMALLINT    NOT NULL, \
        "COMMODITY_PRICE"       INTEGER, \
        "COMMODITY_CNT"         INTEGER, \
        "TOTAL_PRICE_ZEINUKI"   INTEGER, \
        "TOTAL_TAX"             INTEGER, \
        "TOTAL_PRICE"           INTEGER, \
        "WARIHURI_DIV"          VARCHAR(1) NOT NULL, \
        "TRADER_SEIKYU_NO"      varchar(10), \
        "SEIKYU_MONTH"          varchar(2), \
        "REMARK"                VARCHAR(120), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms
ALTER TABLE LEVY_REQUEST_OUTGO_MEISAI_DAT ADD CONSTRAINT PK_LEVY_OUTGO_M PRIMARY KEY (YEAR, OUTGO_L_CD, OUTGO_M_CD, REQUEST_NO, OUTGO_S_CD)
