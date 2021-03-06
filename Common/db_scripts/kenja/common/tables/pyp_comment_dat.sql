-- $Id: 7be141a1358db0f33cfe2e5d70640af3115435f2 $

DROP TABLE PYP_COMMENT_DAT
CREATE TABLE PYP_COMMENT_DAT( \
    YEAR                    VARCHAR(4)    NOT NULL, \
    SEMESTER                VARCHAR(1)    NOT NULL, \
    CLASSCD                 VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND             VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD           VARCHAR(2)    NOT NULL, \
    SUBCLASSCD              VARCHAR(6)    NOT NULL, \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    COMMENT1                VARCHAR(1800)         , \
    COMMENT2                VARCHAR(1800)         , \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN usr1dms index in idx1dms

ALTER TABLE PYP_COMMENT_DAT ADD CONSTRAINT PK_PYP_COMMENT_DAT PRIMARY KEY (YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)
