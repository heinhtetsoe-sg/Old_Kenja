-- $Id: d098592a8ecf40e747279ecae3ac62220279895c $

drop table PROFICIENCY_YMST

create table PROFICIENCY_YMST \
    (YEAR               varchar(4) not null, \
     SEMESTER           varchar(1) not null, \
     PROFICIENCYDIV     varchar(2) not null, \
     PROFICIENCYCD      varchar(4) not null, \
     GRADE              varchar(2) not null, \
     UPDATE_FLG         varchar(1), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PROFICIENCY_YMST add constraint PK_PRO_YMST primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, GRADE)


