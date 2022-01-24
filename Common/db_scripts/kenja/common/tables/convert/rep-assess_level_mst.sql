-- $Id: 5d25f1aff6c24d9a2dca044c3e6487825eaa3eed $

DROP TABLE ASSESS_LEVEL_MST_OLD
RENAME TABLE ASSESS_LEVEL_MST TO ASSESS_LEVEL_MST_OLD
CREATE TABLE ASSESS_LEVEL_MST( \
    YEAR            varchar(4) not null, \
    SEMESTER        varchar(1) not null, \
    TESTKINDCD      varchar(2) not null, \
    TESTITEMCD      varchar(2) not null, \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    CURRICULUM_CD   varchar(2) not null, \
    SUBCLASSCD      varchar(6) not null, \
    DIV             varchar(1) not null, \
    GRADE           varchar(2) not null, \
    HR_CLASS        varchar(3) not null, \
    COURSECD        varchar(1) not null, \
    MAJORCD         varchar(3) not null, \
    COURSECODE      varchar(4) not null, \
    ASSESSLEVEL     smallint not null, \
    ASSESSMARK      varchar(6), \
    ASSESSLOW       decimal, \
    ASSESSHIGH      decimal, \
    PERCENT         DECIMAL(4,1), \
    PERCENT_ALLCNT  smallint, \
    PERCENT_CNT     smallint, \
    STANDARD_ASSESSLOW       decimal, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO ASSESS_LEVEL_MST \
    SELECT \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        DIV, \
        GRADE, \
        HR_CLASS, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        ASSESSLEVEL, \
        ASSESSMARK, \
        ASSESSLOW, \
        ASSESSHIGH, \
        PERCENT, \
        CAST(NULL AS SMALLINT) AS PERCENT_ALLCNT, \
        CAST(NULL AS SMALLINT) AS PERCENT_CNT, \
        STANDARD_ASSESSLOW, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ASSESS_LEVEL_MST_OLD

alter table ASSESS_LEVEL_MST add constraint pk_ass_lvl_mst \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, ASSESSLEVEL)
