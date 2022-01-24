-- $Id: 6f4ba84ffac5969626e372430cd177f2a2690766 $

DROP TABLE UNIT_SCH_DIVISION_DAT_OLD
RENAME TABLE UNIT_SCH_DIVISION_DAT TO UNIT_SCH_DIVISION_DAT_OLD
CREATE TABLE UNIT_SCH_DIVISION_DAT( \
     YEAR                VARCHAR(4)    NOT NULL, \
     EXECUTEDATE         DATE          NOT NULL, \
     PERIODCD            VARCHAR(1)    NOT NULL, \
     HR_NAME             VARCHAR(15), \
     SEQ                 VARCHAR(1), \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     SUBCLASSTIME        VARCHAR(2), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

INSERT INTO UNIT_SCH_DIVISION_DAT \
    SELECT \
        YEAR, \
        EXECUTEDATE, \
        PERIODCD, \
        HR_NAME, \
        SEQ, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        SUBCLASSTIME, \
        REGISTERCD, \
        UPDATED \
    FROM \
        UNIT_SCH_DIVISION_DAT_OLD

alter table UNIT_SCH_DIVISION_DAT add constraint pk_unit_sch_di_dat primary key (YEAR, EXECUTEDATE)