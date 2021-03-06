-- kanji=漢字
-- $Id: a71fc88022d7e4cd4c7547585906d622173170b0 $

DROP TABLE INDUSTRY_L_MST
CREATE TABLE INDUSTRY_L_MST( \
    INDUSTRY_LCD   VARCHAR(1)    NOT NULL, \
    INDUSTRY_LNAME VARCHAR(90), \
    L_GROUPCD      VARCHAR(2), \
    REGISTERCD     VARCHAR(8), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE INDUSTRY_L_MST ADD CONSTRAINT PK_INDUSTRY_L_MST PRIMARY KEY (INDUSTRY_LCD)