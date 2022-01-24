-- $Id: d55d94d3324771b00991e61cc89bc50c969cbaf6 $

DROP TABLE STUDYRECREMARK_DAT_OLD
RENAME TABLE STUDYRECREMARK_DAT TO STUDYRECREMARK_DAT_OLD
CREATE TABLE STUDYRECREMARK_DAT( \
    YEAR            VARCHAR(4)    NOT NULL, \
    SCHREGNO        VARCHAR(8)    NOT NULL, \
    CLASSCD         VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD   VARCHAR(2)    NOT NULL, \
    SUBCLASSCD      VARCHAR(6)    NOT NULL, \
    REMARK          VARCHAR(150), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO STUDYRECREMARK_DAT \
    SELECT \
        YEAR, \
        SCHREGNO, \
        CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        REMARK, \
        REGISTERCD, \
        UPDATED \
    FROM \
        STUDYRECREMARK_DAT_OLD

ALTER TABLE STUDYRECREMARK_DAT ADD CONSTRAINT PK_STUDYCLASS_DAT PRIMARY KEY (YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)