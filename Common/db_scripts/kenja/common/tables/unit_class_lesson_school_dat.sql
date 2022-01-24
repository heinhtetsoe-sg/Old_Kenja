-- $Id: 3cc0453b641fc4819d123b945301819276d29950 $

drop table UNIT_CLASS_LESSON_SCHOOL_DAT

create table UNIT_CLASS_LESSON_SCHOOL_DAT( \
     YEAR                VARCHAR(4)    NOT NULL, \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     SEMESTER            VARCHAR(1)    NOT NULL, \
     GRADE               VARCHAR(2)    NOT NULL, \
     TIME_DIV            VARCHAR(1)    NOT NULL, \
     STANDARD_TIME       VARCHAR(3), \
     USE_CLASS_FRG       VARCHAR(1), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table UNIT_CLASS_LESSON_SCHOOL_DAT add constraint pk_unit_cls_dat primary key (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER, GRADE, TIME_DIV)