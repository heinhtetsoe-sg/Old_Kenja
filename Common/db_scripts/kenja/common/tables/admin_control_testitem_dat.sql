-- $Id: 4b633221e45a9dbc93231fba113f86076a099c8f $

DROP TABLE ADMIN_CONTROL_TESTITEM_DAT
CREATE TABLE ADMIN_CONTROL_TESTITEM_DAT ( \
    YEAR            varchar(4) not null, \
    GRADE           varchar(2) not null, \
    COURSECD        varchar(1) not null, \
    MAJORCD         varchar(3) not null, \
    COURSECODE      varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    CURRICULUM_CD   varchar(2) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SEMESTER        varchar(1) not null, \
    TESTKINDCD      varchar(2) not null, \
    TESTITEMCD      varchar(2) not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_TESTITEM_DAT add constraint PK_AD_TESTITEM primary key (YEAR, GRADE, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD)
