-- $Id: 39457229b3a03fb9a839e635240855df609dc5ca $

DROP TABLE SUBCLASS_ANOTHER_DAT
CREATE TABLE SUBCLASS_ANOTHER_DAT( \
    EDBOARD_SCHOOLCD   VARCHAR(12)   NOT NULL, \
    CLASSCD            VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND        VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD      VARCHAR(2)    NOT NULL, \
    SUBCLASSCD         VARCHAR(6)    NOT NULL, \
    EDBOARD_FLG        VARCHAR(1), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_ANOTHER_DAT ADD CONSTRAINT PK_S_ANOTHER_DAT PRIMARY KEY (EDBOARD_SCHOOLCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)