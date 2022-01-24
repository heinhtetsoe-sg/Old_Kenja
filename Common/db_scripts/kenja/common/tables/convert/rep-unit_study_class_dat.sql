-- $Id: 9d0bebf34d564ad6f0c79015114508ba93e9c561 $

DROP TABLE UNIT_STUDY_CLASS_DAT_OLD
RENAME TABLE UNIT_STUDY_CLASS_DAT TO UNIT_STUDY_CLASS_DAT_OLD
CREATE TABLE UNIT_STUDY_CLASS_DAT( \
     YEAR                VARCHAR(4)    NOT NULL, \
     GRADE               VARCHAR(2)    NOT NULL, \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

INSERT INTO UNIT_STUDY_CLASS_DAT \
    SELECT \
        YEAR, \
        GRADE, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        UNIT_STUDY_CLASS_DAT_OLD

alter table UNIT_STUDY_CLASS_DAT add constraint pk_unit_sc_dat primary key (YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)