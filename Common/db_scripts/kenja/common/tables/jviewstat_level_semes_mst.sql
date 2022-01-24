-- $Id: 39fe16c17a6ddb20a7ee871657759bbb1efa210c $

drop table JVIEWSTAT_LEVEL_SEMES_MST
create table JVIEWSTAT_LEVEL_SEMES_MST ( \
    YEAR            VARCHAR(4)  NOT NULL, \
    SEMESTER        VARCHAR(1)  NOT NULL, \
    CLASSCD         VARCHAR(2)  NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)  NOT NULL, \
    CURRICULUM_CD   VARCHAR(2)  NOT NULL, \
    SUBCLASSCD      VARCHAR(6)  NOT NULL, \
    VIEWCD          VARCHAR(4)  NOT NULL, \
    DIV             VARCHAR(1)  NOT NULL, \
    GRADE           VARCHAR(2)  NOT NULL, \
    ASSESSLEVEL     SMALLINT    NOT NULL, \
    ASSESSMARK      VARCHAR(6), \
    ASSESSLOW       DECIMAL, \
    ASSESSHIGH      DECIMAL, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table JVIEWSTAT_LEVEL_SEMES_MST add constraint PK_JVS_LV_SM_MST \
      primary key (YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD, DIV, GRADE, ASSESSLEVEL)
