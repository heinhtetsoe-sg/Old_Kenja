-- $Id: 4403f3e4bbd1ab90bd86b07cea9bcd65145ea149 $

drop   table PROFICIENCY_EXEC_DAT

create table PROFICIENCY_EXEC_DAT ( \
    CALC_DATE               DATE not null, \
    CALC_TIME               TIME not null, \
    YEAR                    varchar(4), \
    SEMESTER                varchar(1), \
    GRADE                   varchar(2), \
    PROFICIENCYDIV          varchar(2), \
    PROFICIENCYCD           varchar(4), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_EXEC_DAT add constraint PK_PRO_EXEC_DAT primary key (CALC_DATE, CALC_TIME)
