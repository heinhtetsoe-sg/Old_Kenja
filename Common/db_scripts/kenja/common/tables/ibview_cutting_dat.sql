-- $Id: 316b0213a0c7c6af0a15ca6b69848d1f2ab485ac $

DROP TABLE IBVIEW_CUTTING_DAT
CREATE TABLE IBVIEW_CUTTING_DAT( \
    YEAR               VARCHAR(4)    NOT NULL, \
    GRADE              VARCHAR(2)    NOT NULL, \
    IBPRG_COURSE       VARCHAR(2)    NOT NULL, \
    CLASSCD            VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND        VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD      VARCHAR(2)    NOT NULL, \
    SUBCLASSCD         VARCHAR(6)    NOT NULL, \
    DATA_DIV           VARCHAR(1)    NOT NULL, \
    SEQ                SMALLINT      NOT NULL, \
    CUTTING_MARK       VARCHAR (2), \
    CUTTING_LOW        DECIMAL (4,1), \
    CUTTING_HIGH       DECIMAL (4,1), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE IBVIEW_CUTTING_DAT ADD CONSTRAINT PK_IBVIEW_CUTTING PRIMARY KEY (YEAR, GRADE, IBPRG_COURSE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, DATA_DIV, SEQ)