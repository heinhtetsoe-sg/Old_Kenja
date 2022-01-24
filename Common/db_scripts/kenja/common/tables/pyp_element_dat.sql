-- $Id: 913e2b9f57d8cbeefa436533416f02e18886b61d $

DROP TABLE PYP_ELEMENT_DAT
CREATE TABLE PYP_ELEMENT_DAT( \
    YEAR                    VARCHAR(4)    NOT NULL, \
    SEMESTER                VARCHAR(1)    NOT NULL, \
    GRADE                   VARCHAR(2)    NOT NULL, \
    CLASSCD                 VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND             VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD           VARCHAR(2)    NOT NULL, \
    SUBCLASSCD              VARCHAR(6)    NOT NULL, \
    ELEMENT_DIV             VARCHAR(1)    NOT NULL, \
    ELEMENT_CD              VARCHAR(3)    NOT NULL, \
    SORT                    VARCHAR(1)            , \
    REGISTERCD              VARCHAR(10)           , \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN usr1dms index in idx1dms

ALTER TABLE PYP_ELEMENT_DAT ADD CONSTRAINT PK_PYP_ELEMENT_DAT PRIMARY KEY (YEAR, SEMESTER, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ELEMENT_DIV, ELEMENT_CD)
