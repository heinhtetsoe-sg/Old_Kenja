-- $Id: 93b17d82316713c81f0f63dd1627ba2e36cd51fe $

DROP TABLE UNIT_SCH_REMARK_DAT_OLD
RENAME TABLE UNIT_SCH_REMARK_DAT TO UNIT_SCH_REMARK_DAT_OLD
CREATE TABLE UNIT_SCH_REMARK_DAT( \
     YEAR               VARCHAR(4)    NOT NULL, \
     GRADE              VARCHAR(2)    NOT NULL, \
     HR_CLASS           VARCHAR(3)    NOT NULL, \
     CLASSCD            VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND        VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD      VARCHAR(2)    NOT NULL, \
     SUBCLASSCD         VARCHAR(6)    NOT NULL, \
     UNIT_L_NAME        VARCHAR(30), \
     UNIT_M_NAME        VARCHAR(30), \
     UNIT_S_NAME        VARCHAR(30), \
     REMARK1            VARCHAR(90), \
     REMARK2            VARCHAR(90), \
     REMARK3            VARCHAR(90), \
     REMARK4            VARCHAR(90), \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

INSERT INTO UNIT_SCH_REMARK_DAT \
    SELECT \
        YEAR, \
        GRADE, \
        HR_CLASS, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        UNIT_L_NAME, \
        UNIT_M_NAME, \
        UNIT_S_NAME, \
        REMARK1, \
        REMARK2, \
        REMARK3, \
        REMARK4, \
        REGISTERCD, \
        UPDATED \
    FROM \
        UNIT_SCH_REMARK_DAT_OLD

alter table UNIT_SCH_REMARK_DAT add constraint pk_unit_sch_re_dat primary key (YEAR, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)