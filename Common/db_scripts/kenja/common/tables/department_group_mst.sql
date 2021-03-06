-- kanji=漢字
-- $Id: 4b865c1da582cd20281d10fdcf7ae5720e162982 $

DROP TABLE DEPARTMENT_GROUP_MST
CREATE TABLE DEPARTMENT_GROUP_MST( \
    DEPARTMENT_GROUP     VARCHAR(3)    NOT NULL, \
    DEPARTMENT_GROUPNAME VARCHAR(90), \
    REGISTERCD           VARCHAR(8), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DEPARTMENT_GROUP_MST ADD CONSTRAINT PK_DEP_GRP_MST PRIMARY KEY (DEPARTMENT_GROUP)