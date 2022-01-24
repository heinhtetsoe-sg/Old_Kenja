-- $Id: 3f9fdccf683343ce241a9c754306cf9bafa0f787 $

drop table UNIT_SCH_REMARK_DAT

create table UNIT_SCH_REMARK_DAT( \
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

alter table UNIT_SCH_REMARK_DAT add constraint pk_unit_sch_re_dat primary key (YEAR, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)