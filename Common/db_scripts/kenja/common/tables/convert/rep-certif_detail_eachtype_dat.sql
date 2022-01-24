-- $Id$

DROP TABLE CERTIF_DETAIL_EACHTYPE_DAT_OLD
RENAME TABLE CERTIF_DETAIL_EACHTYPE_DAT TO CERTIF_DETAIL_EACHTYPE_DAT_OLD
CREATE TABLE CERTIF_DETAIL_EACHTYPE_DAT( \
    YEAR         VARCHAR(4)    NOT NULL, \
    CERTIF_INDEX VARCHAR(5)    NOT NULL, \
    SCHREGNO     VARCHAR(8), \
    TYPE         VARCHAR(1), \
    REMARK1      VARCHAR(60), \
    REMARK2      VARCHAR(60), \
    REMARK3      VARCHAR(60), \
    REMARK4      VARCHAR(60), \
    REMARK5      VARCHAR(60), \
    REMARK6      VARCHAR(60), \
    REMARK7      VARCHAR(60), \
    REMARK8      VARCHAR(60), \
    REMARK9      VARCHAR(60), \
    REMARK10     VARCHAR(60), \
    REMARK11     VARCHAR(60), \
    REMARK12     VARCHAR(60), \
    REMARK13     VARCHAR(60), \
    REMARK14     VARCHAR(60), \
    REMARK15     VARCHAR(60), \
    REMARK16     VARCHAR(60), \
    REMARK17     VARCHAR(60), \
    REMARK18     VARCHAR(60), \
    REMARK19     VARCHAR(60), \
    REMARK20     VARCHAR(60), \
    REMARK21     VARCHAR(60), \
    REMARK22     VARCHAR(60), \
    REMARK23     VARCHAR(60), \
    REMARK24     VARCHAR(60), \
    REMARK25     VARCHAR(60), \
    REMARK26     VARCHAR(60), \
    REMARK27     VARCHAR(60), \
    REMARK28     VARCHAR(60), \
    REMARK29     VARCHAR(60), \
    REMARK30     VARCHAR(60), \
    REMARK31     VARCHAR(60), \
    REMARK32     VARCHAR(60), \
    REMARK33     VARCHAR(60), \
    REMARK34     VARCHAR(60), \
    REMARK35     VARCHAR(60), \
    REMARK36     VARCHAR(60), \
    REMARK37     VARCHAR(60), \
    REMARK38     VARCHAR(60), \
    REMARK39     VARCHAR(60), \
    REMARK40     VARCHAR(60), \
    REGISTERCD   VARCHAR(10), \
    UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO CERTIF_DETAIL_EACHTYPE_DAT \
    SELECT \
        YEAR, \
        CERTIF_INDEX, \
        SCHREGNO, \
        TYPE, \
        REMARK1, \
        REMARK2, \
        REMARK3, \
        REMARK4, \
        REMARK5, \
        REMARK6, \
        REMARK7, \
        REMARK8, \
        REMARK9, \
        REMARK10, \
        REMARK11, \
        REMARK12, \
        REMARK13, \
        REMARK14, \
        REMARK15, \
        REMARK16, \
        REMARK17, \
        REMARK18, \
        REMARK19, \
        REMARK20, \
        CAST(NULL AS VARCHAR(1)) AS REMARK21, \
        CAST(NULL AS VARCHAR(1)) AS REMARK22, \
        CAST(NULL AS VARCHAR(1)) AS REMARK23, \
        CAST(NULL AS VARCHAR(1)) AS REMARK24, \
        CAST(NULL AS VARCHAR(1)) AS REMARK25, \
        CAST(NULL AS VARCHAR(1)) AS REMARK26, \
        CAST(NULL AS VARCHAR(1)) AS REMARK27, \
        CAST(NULL AS VARCHAR(1)) AS REMARK28, \
        CAST(NULL AS VARCHAR(1)) AS REMARK29, \
        CAST(NULL AS VARCHAR(1)) AS REMARK30, \
        CAST(NULL AS VARCHAR(1)) AS REMARK31, \
        CAST(NULL AS VARCHAR(1)) AS REMARK32, \
        CAST(NULL AS VARCHAR(1)) AS REMARK33, \
        CAST(NULL AS VARCHAR(1)) AS REMARK34, \
        CAST(NULL AS VARCHAR(1)) AS REMARK35, \
        CAST(NULL AS VARCHAR(1)) AS REMARK36, \
        CAST(NULL AS VARCHAR(1)) AS REMARK37, \
        CAST(NULL AS VARCHAR(1)) AS REMARK38, \
        CAST(NULL AS VARCHAR(1)) AS REMARK39, \
        CAST(NULL AS VARCHAR(1)) AS REMARK40, \
        REGISTERCD, \
        UPDATED \
    FROM \
        CERTIF_DETAIL_EACHTYPE_DAT_OLD

ALTER TABLE CERTIF_DETAIL_EACHTYPE_DAT ADD CONSTRAINT PK_CERTIF_D_E_DAT PRIMARY KEY (YEAR,CERTIF_INDEX)