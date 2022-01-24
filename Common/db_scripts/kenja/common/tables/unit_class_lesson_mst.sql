-- $Id: ab44cbabd7732a3058c6ce80d8798c21906c7ce5 $

drop table UNIT_CLASS_LESSON_MST

create table UNIT_CLASS_LESSON_MST( \
     YEAR                VARCHAR(4)    NOT NULL, \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     GRADE               VARCHAR(2)    NOT NULL, \
     STANDARD_TIME       VARCHAR(3), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table UNIT_CLASS_LESSON_MST add constraint pk_unit_cls_ls_mst primary key (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, GRADE)