-- $Id: f27cf241f0d15b568fabc223aa54beb9af3c5a65 $
drop table PROFICIENCY_SUBCLASS_GROUP_MST

create table PROFICIENCY_SUBCLASS_GROUP_MST ( \
    YEAR            varchar(4) not null, \
    SEMESTER        varchar(1) not null, \
    PROFICIENCYDIV  varchar(2) not null, \
    PROFICIENCYCD   varchar(4) not null, \
    GROUP_DIV       varchar(2) not null, \
    GRADE           varchar(2) not null, \
    COURSECD        varchar(1) not null, \
    MAJORCD         varchar(3) not null, \
    COURSECODE      varchar(4) not null, \
    GROUP_NAME      varchar(30), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
)  in usr1dms index in idx1dms

alter table PROFICIENCY_SUBCLASS_GROUP_MST add constraint PK_PRO_SUB_GP_MST \
primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, GROUP_DIV, GRADE, COURSECD, MAJORCD, COURSECODE)

