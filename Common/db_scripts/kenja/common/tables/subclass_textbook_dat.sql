-- $Id: febeb0a059f83d22bf826cc5cca45ca846783cc6 $

drop table SUBCLASS_TEXTBOOK_DAT
create table SUBCLASS_TEXTBOOK_DAT ( \
     YEAR           varchar(4)  not null, \
     CLASSCD        varchar(2)  not null, \
     SCHOOL_KIND    varchar(2)  not null, \
     CURRICULUM_CD  varchar(2)  not null, \
     SUBCLASSCD     varchar(6)  not null, \
     TEXTBOOKCD     varchar(12) not null, \
     NOT_DEFAULT    varchar(1), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SUBCLASS_TEXTBOOK_DAT add constraint pk_textbook_dat primary key(YEAR,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,TEXTBOOKCD)
