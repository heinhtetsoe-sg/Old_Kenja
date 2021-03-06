-- kanji=????
-- $Id: 1cec29d85edd5f29c04fbc4a9ebb16dad58ef59d $

DROP TABLE COURSE_GROUP_DAT
CREATE TABLE COURSE_GROUP_DAT( \
    YEAR       VARCHAR(4)    NOT NULL, \
    GRADE      VARCHAR(2)    NOT NULL, \
    GROUP_CD   VARCHAR(3)    NOT NULL, \
    COURSECD   VARCHAR(1)    NOT NULL, \
    MAJORCD    VARCHAR(3)    NOT NULL, \
    COURSECODE VARCHAR(4)    NOT NULL, \
    REGISTERCD VARCHAR(10), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COURSE_GROUP_DAT ADD CONSTRAINT PK_COURSE_GRP_DAT PRIMARY KEY (YEAR,GRADE,GROUP_CD,COURSECD,MAJORCD,COURSECODE)