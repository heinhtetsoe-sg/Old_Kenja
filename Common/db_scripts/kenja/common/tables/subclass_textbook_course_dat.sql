-- $Id: 3f65a378302edaeec56074fbe78925c866c2f4a2 $

drop table SUBCLASS_TEXTBOOK_COURSE_DAT
create table SUBCLASS_TEXTBOOK_COURSE_DAT ( \
     YEAR           varchar(4)  not null, \
     CLASSCD        varchar(2)  not null, \
     SCHOOL_KIND    varchar(2)  not null, \
     CURRICULUM_CD  varchar(2)  not null, \
     SUBCLASSCD     varchar(6)  not null, \
     COURSECD       varchar(1)  not null, \
     MAJORCD        varchar(3)  not null, \
     COURSECODE     varchar(4)  not null, \
     TEXTBOOKCD     varchar(12) not null, \
     NOT_DEFAULT    varchar(1), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SUBCLASS_TEXTBOOK_COURSE_DAT add constraint PK_SUB_TEXT_COURSE primary key(YEAR,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,COURSECD,MAJORCD,COURSECODE,TEXTBOOKCD)
