-- $Id: rep-katei_mst.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KATEI_MST_OLD
RENAME TABLE KATEI_MST TO KATEI_MST_OLD
CREATE TABLE KATEI_MST( \
    KATEI                   VARCHAR(2)   NOT NULL, \
    KATEI_NAME              VARCHAR(60), \
    SHUUGAKU_MONTH_COUNT    SMALLINT, \
    KATEI_DIV               VARCHAR(1), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO KATEI_MST \
    SELECT \
        KATEI                , \
        KATEI_NAME           , \
        SHUUGAKU_MONTH_COUNT , \
        CAST(NULL AS VARCHAR(1)) AS KATEI_DIV, \
        REGISTERCD, \
        UPDATED \
    FROM \
        KATEI_MST_OLD
ALTER TABLE KATEI_MST ADD CONSTRAINT PK_KATEI_MST PRIMARY KEY (KATEI)