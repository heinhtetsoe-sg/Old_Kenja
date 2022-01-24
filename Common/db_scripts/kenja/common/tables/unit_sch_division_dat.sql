-- $Id: c6d377d52815b003b5cccdd599ca8cfd0e6f52c9 $

drop table UNIT_SCH_DIVISION_DAT

create table UNIT_SCH_DIVISION_DAT( \
     YEAR                VARCHAR(4)    NOT NULL, \
     EXECUTEDATE         DATE          NOT NULL, \
     PERIODCD            VARCHAR(1)    NOT NULL, \
     HR_NAME             VARCHAR(15), \
     SEQ                 VARCHAR(1), \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     SUBCLASSTIME        VARCHAR(2), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table UNIT_SCH_DIVISION_DAT add constraint pk_unit_sch_di_dat primary key (YEAR, EXECUTEDATE)