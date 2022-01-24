-- $Id: 88c5217ce4d35c51094a3a6db6a5abdffbf8a8e8 $

drop table UNIT_MST

create table UNIT_MST( \
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

alter table UNIT_MST add constraint pk_unit_mst primary key (YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ISSUECOMPANYCD, SEQ)