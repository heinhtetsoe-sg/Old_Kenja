-- $Id: 4b713022c5112224ad3f57569021cf8cbb935a33 $

drop table UNIT_STUDY_CLASS_DAT

create table UNIT_STUDY_CLASS_DAT( \
     YEAR                VARCHAR(4)    NOT NULL, \
     GRADE               VARCHAR(2)    NOT NULL, \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table UNIT_STUDY_CLASS_DAT add constraint pk_unit_sc_dat primary key (YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)