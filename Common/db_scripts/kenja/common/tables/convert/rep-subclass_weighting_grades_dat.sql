-- $Id: 1432eb776eb16910114e3e3fdccf4d317da71293 $

DROP TABLE SUBCLASS_WEIGHTING_GRADES_DAT_OLD
RENAME TABLE SUBCLASS_WEIGHTING_GRADES_DAT TO SUBCLASS_WEIGHTING_GRADES_DAT_OLD
CREATE TABLE SUBCLASS_WEIGHTING_GRADES_DAT( \
    YEAR                      VARCHAR(4) NOT NULL, \
    GRADE                     VARCHAR(2) NOT NULL, \
    COMBINED_CLASSCD          VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND      VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD    VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD       VARCHAR(6) NOT NULL, \
    ATTEND_CLASSCD            VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    WEIGHTING                 SMALLINT, \
    REGISTERCD                VARCHAR(8), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

INSERT INTO SUBCLASS_WEIGHTING_GRADES_DAT \
    SELECT \
        YEAR, \
        GRADE, \
        LEFT(COMBINED_SUBCLASSCD, 2) AS COMBINED_CLASSCD, \
        'H' AS COMBINED_SCHOOL_KIND, \
        '2' AS COMBINED_CURRICULUM_CD, \
        COMBINED_SUBCLASSCD, \
        LEFT(ATTEND_SUBCLASSCD, 2) AS ATTEND_CLASSCD, \
        'H' AS ATTEND_SCHOOL_KIND, \
        '2' AS ATTEND_CURRICULUM_CD, \
        ATTEND_SUBCLASSCD, \
        WEIGHTING, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SUBCLASS_WEIGHTING_GRADES_DAT_OLD

alter table SUBCLASS_WEIGHTING_GRADES_DAT add constraint PK_SUBWEIGHTING_D \
        primary key (YEAR, GRADE, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
