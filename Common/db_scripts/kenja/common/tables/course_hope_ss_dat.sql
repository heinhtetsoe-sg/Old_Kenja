-- kanji=漢字

DROP TABLE COURSE_HOPE_SS_DAT
CREATE TABLE COURSE_HOPE_SS_DAT( \
    ENTRYDATE         DATE          NOT NULL, \
    SEQ               INTEGER       NOT NULL, \
    SCHREGNO          VARCHAR(8)    NOT NULL, \
    COURSE_KIND       VARCHAR(1)    NOT NULL, \
    QUESTIONNAIRECD   VARCHAR(2), \
    SCHOOL_GROUP1     VARCHAR(2), \
    FACULTY_GROUP1    VARCHAR(3), \
    DEPARTMENT_GROUP1 VARCHAR(3), \
    SCHOOL_CD1        VARCHAR(12), \
    FACULTYCD1        VARCHAR(3), \
    DEPARTMENTCD1     VARCHAR(3), \
    HOWTOEXAM1        INTEGER, \
    SCHOOL_GROUP2     VARCHAR(2), \
    FACULTY_GROUP2    VARCHAR(3), \
    DEPARTMENT_GROUP2 VARCHAR(3), \
    SCHOOL_CD2        VARCHAR(12), \
    FACULTYCD2        VARCHAR(3), \
    DEPARTMENTCD2     VARCHAR(3), \
    HOWTOEXAM2        INTEGER, \
    JOBTYPE_LCD1      VARCHAR(2), \
    JOBTYPE_MCD1      VARCHAR(2), \
    JOBTYPE_SCD1      VARCHAR(3), \
    JOBTYPE_SSCD1     VARCHAR(2), \
    WORK_AREA1        VARCHAR(1), \
    INTRODUCTION_DIV1 VARCHAR(1), \
    JOBTYPE_LCD2      VARCHAR(2), \
    JOBTYPE_MCD2      VARCHAR(2), \
    JOBTYPE_SCD2      VARCHAR(3), \
    JOBTYPE_SSCD2     VARCHAR(2), \
    WORK_AREA2        VARCHAR(1), \
    INTRODUCTION_DIV2 VARCHAR(1), \
    REMARK            VARCHAR(500), \
    YEAR              VARCHAR(4)    NOT NULL, \
    REGISTERCD        VARCHAR(10), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COURSE_HOPE_SS_DAT ADD CONSTRAINT PK_COURSE_HOPE_SS_DAT PRIMARY KEY (ENTRYDATE,SEQ,SCHREGNO)