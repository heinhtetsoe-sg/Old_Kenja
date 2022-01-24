-- $Id: cfd4f29ad2265dbc22d8e46a84c646e1b4b254b0 $

DROP TABLE UNIT_MST_OLD
RENAME TABLE UNIT_MST TO UNIT_MST_OLD
CREATE TABLE UNIT_MST( \
     YEAR                   VARCHAR(4)    NOT NULL, \
     GRADE                  VARCHAR(2)    NOT NULL, \
     CLASSCD                VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND            VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD          VARCHAR(2)    NOT NULL, \
     SUBCLASSCD             VARCHAR(6)    NOT NULL, \
     ISSUECOMPANYCD         VARCHAR(4)    NOT NULL, \
     SEQ                    SMALLINT      NOT NULL, \
     SYUPPAN                VARCHAR(45), \
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
     REGISTERCD             VARCHAR(8), \
     UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

INSERT INTO UNIT_MST \
    SELECT \
        YEAR, \
        GRADE, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        ISSUECOMPANYCD, \
        SEQ, \
        SYUPPAN, \
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
        REGISTERCD, \
        UPDATED \
    FROM \
        UNIT_MST_OLD

alter table UNIT_MST add constraint pk_unit_dat primary key (YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ISSUECOMPANYCD, SEQ)