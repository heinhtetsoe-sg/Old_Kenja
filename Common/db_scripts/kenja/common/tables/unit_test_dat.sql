-- $Id: 63d9b7e97b20715da410c836f837375048d635b5 $

drop table UNIT_TEST_DAT

create table UNIT_TEST_DAT( \
     YEAR                   VARCHAR(4)    NOT NULL, \
     DATA_DIV               VARCHAR(1)    NOT NULL, \
     GRADE                  VARCHAR(2)    NOT NULL, \
     HR_CLASS               VARCHAR(3)    NOT NULL, \
     CLASSCD                VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND            VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD          VARCHAR(2)    NOT NULL, \
     SUBCLASSCD             VARCHAR(6)    NOT NULL, \
     SEQ                    SMALLINT      NOT NULL, \
     SORT                   SMALLINT      NOT NULL, \
     UNIT_L_NAME            VARCHAR(500), \
     UNIT_M_NAME            VARCHAR(90), \
     UNIT_S_NAME            VARCHAR(90), \
     UNIT_DATA              VARCHAR(90), \
     SEMESTER               VARCHAR(1), \
     UNIT_TEST_DATE         DATE   , \
     REGISTERCD             VARCHAR(10), \
     UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table UNIT_TEST_DAT add constraint pk_unit_test_dat primary key (YEAR, DATA_DIV, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEQ)