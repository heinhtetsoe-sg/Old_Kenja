-- $Id: schreg_textbook_free_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_TEXTBOOK_FREE_DAT
create table SCHREG_TEXTBOOK_FREE_DAT ( \
     SCHREGNO       varchar(8)  not null, \
     YEAR           varchar(4)  not null, \
     REGISTER_DATE  DATE        not null, \
     CLASSCD        varchar(2)  not null, \
     SCHOOL_KIND    varchar(2)  not null, \
     CURRICULUM_CD  varchar(2)  not null, \
     SUBCLASSCD     varchar(6)  not null, \
     TEXTBOOKCD     varchar(12) not null, \
     FREE_FLG       varchar(1), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SCHREG_TEXTBOOK_FREE_DAT add constraint PK_SCH_TEXT_F_DAT primary key(SCHREGNO, YEAR, REGISTER_DATE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TEXTBOOKCD)
