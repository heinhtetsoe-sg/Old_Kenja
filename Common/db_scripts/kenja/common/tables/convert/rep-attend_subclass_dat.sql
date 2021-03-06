-- $Id: 89f5b21ff5a00ea2e9dd2e3280e7a77eed8a3d76 $

drop table ATTEND_SUBCLASS_DAT_OLD
create table ATTEND_SUBCLASS_DAT_OLD like ATTEND_SUBCLASS_DAT
insert into  ATTEND_SUBCLASS_DAT_OLD select * from ATTEND_SUBCLASS_DAT

drop   table ATTEND_SUBCLASS_DAT
create table ATTEND_SUBCLASS_DAT ( \
    COPYCD        VARCHAR(1)    NOT NULL, \
    YEAR          VARCHAR(4)    NOT NULL, \
    MONTH         VARCHAR(2)    NOT NULL, \
    SEMESTER      VARCHAR(1)    NOT NULL, \
    SCHREGNO      VARCHAR(8)    NOT NULL, \
    CLASSCD       VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND   VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD VARCHAR(2)    NOT NULL, \
    SUBCLASSCD    VARCHAR(6)    NOT NULL, \
    APPOINTED_DAY VARCHAR(2), \
    LESSON        SMALLINT, \
    OFFDAYS       SMALLINT, \
    ABSENT        SMALLINT, \
    SUSPEND       SMALLINT, \
    MOURNING      SMALLINT, \
    ABROAD        SMALLINT, \
    SICK          SMALLINT, \
    NOTICE        SMALLINT, \
    NONOTICE      SMALLINT, \
    NURSEOFF      SMALLINT, \
    LATE          SMALLINT, \
    EARLY         SMALLINT, \
    VIRUS         SMALLINT, \
    KOUDOME       SMALLINT, \
    REGISTERCD    VARCHAR(10), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SUBCLASS_DAT ADD CONSTRAINT PK_AT_SUB_DAT PRIMARY KEY (COPYCD,YEAR,MONTH,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)

insert into ATTEND_SUBCLASS_DAT \
    SELECT \
        COPYCD, \
        YEAR, \
        MONTH, \
        SEMESTER, \
        SCHREGNO, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        APPOINTED_DAY, \
        LESSON, \
        OFFDAYS, \
        ABSENT, \
        SUSPEND, \
        MOURNING, \
        ABROAD, \
        SICK, \
        NOTICE, \
        NONOTICE, \
        NURSEOFF, \
        LATE, \
        EARLY, \
        VIRUS, \
        CAST(NULL AS SMALLINT) AS KOUDOME, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ATTEND_SUBCLASS_DAT_OLD