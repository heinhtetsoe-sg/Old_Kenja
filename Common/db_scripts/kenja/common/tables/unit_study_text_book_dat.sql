-- $Id: f036225c18cbff1434a1ab1ff6845e740aa72c6d $

drop table UNIT_STUDY_TEXT_BOOK_DAT

create table UNIT_STUDY_TEXT_BOOK_DAT( \
     YEAR                VARCHAR(4)    NOT NULL, \
     GRADE               VARCHAR(2)    NOT NULL, \
     CLASSCD             VARCHAR(2)    NOT NULL, \
     SCHOOL_KIND         VARCHAR(2)    NOT NULL, \
     CURRICULUM_CD       VARCHAR(2)    NOT NULL, \
     SUBCLASSCD          VARCHAR(6)    NOT NULL, \
     ISSUECOMPANYCD      VARCHAR(4), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table UNIT_STUDY_TEXT_BOOK_DAT add constraint pk_unit_stb_dat primary key (YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)