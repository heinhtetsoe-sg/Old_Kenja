-- kanji=漢字
-- $Id: 714ae77a581495de1e11d0d0284ddeb37026746a $


DROP TABLE COLLEGE_DAT_OLD
CREATE TABLE COLLEGE_DAT_OLD LIKE COLLEGE_DAT
INSERT INTO COLLEGE_DAT_OLD SELECT * FROM COLLEGE_DAT
DROP TABLE COLLEGE_DAT
CREATE TABLE COLLEGE_DAT( \
    SCHOOL_CD    VARCHAR(8)    NOT NULL, \
    FACULTYCD    VARCHAR(3)    NOT NULL, \
    DEPARTMENTCD VARCHAR(3)    NOT NULL, \
    COURSECD     VARCHAR(1), \
    REGISTERCD   VARCHAR(8), \
    UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS


INSERT INTO COLLEGE_DAT \
    SELECT \
        SCHOOL_CD, \
        FACULTYCD, \
        DEPARTMENTCD, \
        COURSECD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        COLLEGE_DAT_OLD
ALTER TABLE COLLEGE_DAT ADD CONSTRAINT PK_COLLEGE_DAT PRIMARY KEY (SCHOOL_CD,FACULTYCD,DEPARTMENTCD)