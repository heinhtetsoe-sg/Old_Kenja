-- $Id: a72f6f1ff92f0e7e5b1085a6d14a6fcdcf5f8c7c $

DROP TABLE COMP_GROUP_MST

CREATE TABLE COMP_GROUP_MST( \
    GAKUBU_SCHOOL_KIND  VARCHAR(2) NOT NULL, \
    CONDITION           VARCHAR(1) NOT NULL, \
    GROUPCD             VARCHAR(4) NOT NULL, \
    GROUPNAME           VARCHAR(90), \
    GROUPABBV           VARCHAR(90), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COMP_GROUP_MST ADD CONSTRAINT PK_COMP_GROUP_MST PRIMARY KEY (GAKUBU_SCHOOL_KIND, CONDITION, GROUPCD)