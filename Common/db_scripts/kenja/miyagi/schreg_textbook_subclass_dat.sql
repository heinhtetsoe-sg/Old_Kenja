-- $Id: schreg_textbook_subclass_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table schreg_textbook_subclass_dat

create table schreg_textbook_subclass_dat \
      ("SCHREGNO"       varchar(8)  not null, \
       "YEAR"           varchar(4)  not null, \
       "CLASSCD"        varchar(2)  not null, \
       "SCHOOL_KIND"    varchar(2)  not null, \
       "CURRICULUM_CD"  varchar(2)  not null, \
       "SUBCLASSCD"     varchar(6)  not null, \
       "TEXTBOOKCD"     varchar(12) not null, \
       "REGISTERCD"     varchar(8), \
       "UPDATED"        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table schreg_textbook_subclass_dat add constraint pk_sch_textbk_sub primary key \
      (SCHREGNO, YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TEXTBOOKCD)
