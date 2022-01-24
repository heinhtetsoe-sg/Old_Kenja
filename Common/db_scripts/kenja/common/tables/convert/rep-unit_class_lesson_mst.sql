-- $Id: 65f32e6521dcf04b5abdd3fee822c4cacf7bf17f $

DROP TABLE UNIT_CLASS_LESSON_MST_OLD
RENAME TABLE UNIT_CLASS_LESSON_MST TO UNIT_CLASS_LESSON_MST_OLD
CREATE TABLE UNIT_CLASS_LESSON_MST( \
     YEAR                VARCHAR(4)    NOT NULL, \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     GRADE               VARCHAR(2)    NOT NULL, \
     STANDARD_TIME       VARCHAR(3), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO UNIT_CLASS_LESSON_MST \
    SELECT \
        YEAR, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        GRADE, \
        STANDARD_TIME, \
        REGISTERCD, \
        UPDATED \
    FROM \
        UNIT_CLASS_LESSON_MST_OLD

alter table UNIT_CLASS_LESSON_MST add constraint pk_unit_cls_ls_mst primary key (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, GRADE)