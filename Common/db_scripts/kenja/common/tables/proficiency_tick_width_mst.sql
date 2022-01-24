drop table PROFICIENCY_TICK_WIDTH_MST

create table PROFICIENCY_TICK_WIDTH_MST \
    (YEAR                       varchar(4) not null, \
     PROFICIENCYDIV             varchar(2) not null, \
     PROFICIENCYCD              varchar(4) not null, \
     PROFICIENCY_SUBCLASS_CD    varchar(6) not null, \
     DIV                        varchar(1) not null, \
     GRADE                      varchar(2) not null, \
     HR_CLASS                   varchar(3) not null, \
     COURSECD                   varchar(1) not null, \
     MAJORCD                    varchar(3) not null, \
     COURSECODE                 varchar(4) not null, \
     TICK_STAGE                 smallint not null, \
     TICK_LOW                   smallint, \
     TICK_HIGH                  smallint, \
     REGISTERCD                 varchar(10), \
     UPDATED                    timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PROFICIENCY_TICK_WIDTH_MST add constraint PK_PRO_TICK_WIDTH primary key (YEAR, PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, TICK_STAGE)
