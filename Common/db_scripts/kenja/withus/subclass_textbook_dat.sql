-- $Id: subclass_textbook_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SUBCLASS_TEXTBOOK_DAT
create table SUBCLASS_TEXTBOOK_DAT ( \
     YEAR           varchar(4) not null, \
     CLASSCD        varchar(2) not null, \
     CURRICULUM_CD  varchar(1) not null, \
     SUBCLASSCD     varchar(6) not null, \
     TEXTBOOKCD     varchar(8) not null, \
     NOT_DEFAULT    varchar(1), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SUBCLASS_TEXTBOOK_DAT add constraint pk_textbook_dat primary key(YEAR,CLASSCD,CURRICULUM_CD,SUBCLASSCD,TEXTBOOKCD)
