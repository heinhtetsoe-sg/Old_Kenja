-- $Id: school_div_mst.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE SCHOOL_DIV_MST
CREATE TABLE SCHOOL_DIV_MST( \
    SCHOOL_DIV       				VARCHAR(2)   NOT NULL, \
    SCHOOL_DIV_NAME					VARCHAR(60), \
    SHUUGAKUKIN_TEKIYOU_FLG	 		VARCHAR(1), \
    SHITAKUKIN_TEKIYOU_FLG	 		VARCHAR(1), \
    SHUUGAKUKIN_RISHI_TEKIYOU_FLG	VARCHAR(1), \
    SHITAKUKIN_RISHI_TEKIYOU_FLG	VARCHAR(1), \
    REGISTERCD       				VARCHAR(8), \
    UPDATED          				TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHOOL_DIV_MST ADD CONSTRAINT PK_SCHOOL_DIV_MST PRIMARY KEY (SCHOOL_DIV)