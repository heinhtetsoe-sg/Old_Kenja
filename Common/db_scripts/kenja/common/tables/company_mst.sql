-- kanji=漢字
-- $Id: 7bbaf8483561ffbb40a2ab449e0be201945ad2d9 $

-- 会社マスタ
-- 作成日: 2006/06/14 15:21:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

DROP TABLE COMPANY_MST
CREATE TABLE COMPANY_MST( \
    COMPANY_CD    CHAR(8)       NOT NULL, \
    COMPANY_NAME  VARCHAR(120), \
    SHUSHOKU_ADDR VARCHAR(120), \
    SHIHONKIN     VARCHAR(17), \
    SONINZU       INTEGER, \
    TONINZU       INTEGER, \
    INDUSTRY_LCD  VARCHAR(1), \
    INDUSTRY_MCD  VARCHAR(2), \
    COMPANY_SORT  CHAR(2), \
    TARGET_SEX    CHAR(1), \
    ZIPCD         VARCHAR(8), \
    ADDR1         VARCHAR(90), \
    ADDR2         VARCHAR(90), \
    TELNO         VARCHAR(16), \
    REMARK        VARCHAR(120), \
    REGISTERCD    VARCHAR(8), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COMPANY_MST ADD CONSTRAINT PK_COMPANY_MST PRIMARY KEY (COMPANY_CD)
