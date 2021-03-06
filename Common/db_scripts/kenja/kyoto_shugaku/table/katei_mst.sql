-- $Id: katei_mst.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KATEI_MST
CREATE TABLE KATEI_MST( \
    KATEI                   VARCHAR(2)   NOT NULL, \
    KATEI_NAME              VARCHAR(60), \
    SHUUGAKU_MONTH_COUNT    SMALLINT, \
    KATEI_DIV               VARCHAR(1), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KATEI_MST ADD CONSTRAINT PK_KATEI_MST PRIMARY KEY (KATEI)