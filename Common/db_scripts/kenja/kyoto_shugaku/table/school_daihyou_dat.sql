-- $Id: school_daihyou_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE SCHOOL_DAIHYOU_DAT
CREATE TABLE SCHOOL_DAIHYOU_DAT( \
	YEAR			 VARCHAR(4)    NOT NULL, \
    SCHOOLCD         VARCHAR(7)    NOT NULL, \
    DAIHYOU_NAME1    VARCHAR(120), \
    DAIHYOU_NAME2    VARCHAR(120), \
    DAIHYOU_NAME3    VARCHAR(120), \
    DAIHYOU_NAME4    VARCHAR(120), \
    DAIHYOU_NAME5    VARCHAR(120), \
    REGISTERCD       VARCHAR(8), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHOOL_DAIHYOU_DAT ADD CONSTRAINT PK_S_DAIHYOU_DAT PRIMARY KEY (YEAR, SCHOOLCD)