-- $Id: 175fac93c8ea8891193dcb1b7fa4e65050612a26 $

drop table PROFICIENCY_MST

create table PROFICIENCY_MST \
    (PROFICIENCYDIV     varchar(2) not null, \
     PROFICIENCYCD      varchar(4) not null, \
     PROFICIENCYNAME1   varchar(60), \
     PROFICIENCYNAME2   varchar(60), \
     PROFICIENCYNAME3   varchar(60), \
     COUNTFLG           varchar(1), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PROFICIENCY_MST add constraint PK_PRO_MST primary key (PROFICIENCYDIV, PROFICIENCYCD)


