-- $Id: e20b618834cdb7ef6057909f8565a8b9784360c4 $

DROP TABLE UNIT_DAT_OLD
RENAME TABLE UNIT_DAT TO UNIT_DAT_OLD
CREATE TABLE UNIT_DAT( \
     YEAR                   VARCHAR(4)    NOT NULL, \
     DATA_DIV               VARCHAR(1)    NOT NULL, \
     GRADE                  VARCHAR(2)    NOT NULL, \
     HR_CLASS               VARCHAR(3)    NOT NULL, \
     CLASSCD                VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND            VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD          VARCHAR(2)    NOT NULL, \
     SUBCLASSCD             VARCHAR(6)    NOT NULL, \
     ISSUECOMPANYCD         VARCHAR(4)    NOT NULL, \
     SEQ                    SMALLINT      NOT NULL, \
     DIV                    VARCHAR(1) , \
     ALLOTMENT_MONTH        VARCHAR(2) , \
     L_TITOL                SMALLINT   , \
     UNIT_L_NAME            VARCHAR(90), \
     UNIT_M_NAME            VARCHAR(90), \
     UNIT_S_NAME            VARCHAR(90), \
     UNIT_DATA              VARCHAR(90), \
     ALLOTMENT_TIME         VARCHAR(2), \
     UNIT_ARRIVAL_TARGET    VARCHAR(450), \
     UNIT_DIV               VARCHAR(1), \
     TEXT_PAGE              VARCHAR(4), \
     STAFFCD                VARCHAR(8), \
     REMARK1                VARCHAR(90), \
     REMARK2                VARCHAR(90), \
     REMARK3                VARCHAR(90), \
     REMARK4                VARCHAR(90), \
     REGISTERCD             VARCHAR(8), \
     UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

INSERT INTO UNIT_DAT \
    SELECT \
        YEAR, \
        DATA_DIV, \
        GRADE, \
        HR_CLASS, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        ISSUECOMPANYCD, \
        SEQ, \
        DIV, \
        ALLOTMENT_MONTH, \
        L_TITOL, \
        UNIT_L_NAME, \
        UNIT_M_NAME, \
        UNIT_S_NAME, \
        UNIT_DATA, \
        ALLOTMENT_TIME, \
        UNIT_ARRIVAL_TARGET, \
        UNIT_DIV, \
        TEXT_PAGE, \
        STAFFCD, \
        REMARK1, \
        REMARK2, \
        REMARK3, \
        REMARK4, \
        REGISTERCD, \
        UPDATED \
    FROM \
        UNIT_DAT_OLD

alter table UNIT_DAT add constraint pk_unit_dat primary key (YEAR, DATA_DIV, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ISSUECOMPANYCD, SEQ)