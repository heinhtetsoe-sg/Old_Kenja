-- $Id: c4907f8a295e5f741420d17f4221ef81d5568d7a $

CREATE TABLE HEXAM_PHYSICAL_PERCENTILE_DAT( \
    YEAR          VARCHAR(4)    NOT NULL, \
    DATA_DIV      VARCHAR(1)    NOT NULL, \
    SEX           VARCHAR(1)    NOT NULL, \
    PERCENTILE    DECIMAL(4,1)  NOT NULL, \
    NENREI_YEAR   SMALLINT      NOT NULL, \
    NENREI_MONTH  SMALLINT      NOT NULL, \
    VALUE         DECIMAL(4,1) , \
    REGISTERCD    VARCHAR(10)  , \
    UPDATED       TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HEXAM_PHYSICAL_PERCENTILE_DAT ADD CONSTRAINT PK_HEXAM_P_PCT_DAT PRIMARY KEY (YEAR,DATA_DIV,SEX,PERCENTILE,NENREI_YEAR,NENREI_MONTH)

