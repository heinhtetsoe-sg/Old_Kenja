-- $Id: 5908875872dff8e2edaa47efe6ff6b527f94b813 $

drop table CREDIT_MST_OLD
create table CREDIT_MST_OLD like CREDIT_MST
insert into  CREDIT_MST_OLD select * from CREDIT_MST

drop   table CREDIT_MST
create table CREDIT_MST ( \
    YEAR                 VARCHAR(4)    NOT NULL, \
    COURSECD             VARCHAR(1)    NOT NULL, \
    MAJORCD              VARCHAR(3)    NOT NULL, \
    GRADE                VARCHAR(2)    NOT NULL, \
    COURSECODE           VARCHAR(4)    NOT NULL, \
    CLASSCD              VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND          VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD        VARCHAR(2)    NOT NULL, \
    SUBCLASSCD           VARCHAR(6)    NOT NULL, \
    CREDITS              SMALLINT, \
    ABSENCE_HIGH         DECIMAL(4,1), \
    GET_ABSENCE_HIGH     DECIMAL(4,1), \
    ABSENCE_WARN         SMALLINT, \
    ABSENCE_WARN2        SMALLINT, \
    ABSENCE_WARN3        SMALLINT, \
    REQUIRE_FLG          VARCHAR(1), \
    AUTHORIZE_FLG        VARCHAR(1), \
    COMP_UNCONDITION_FLG VARCHAR(1), \
    TIME_UNIT            DECIMAL(4,1), \
    REGISTERCD           VARCHAR(10), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    RATE                 DECIMAL(2,1) \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CREDIT_MST ADD CONSTRAINT PK_CREDIT_MST PRIMARY KEY (YEAR,COURSECD,MAJORCD,GRADE,COURSECODE,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)

insert into CREDIT_MST \
    SELECT \
        YEAR, \
        COURSECD, \
        MAJORCD, \
        GRADE, \
        COURSECODE, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        CREDITS, \
        ABSENCE_HIGH, \
        GET_ABSENCE_HIGH, \
        ABSENCE_WARN, \
        ABSENCE_WARN AS ABSENCE_WARN2, \
        ABSENCE_WARN AS ABSENCE_WARN3, \
        REQUIRE_FLG, \
        AUTHORIZE_FLG, \
        COMP_UNCONDITION_FLG, \
        TIME_UNIT, \
        REGISTERCD, \
        UPDATED, \
        cast(null as decimal(2,1)) AS RATE \
    FROM \
        CREDIT_MST_OLD