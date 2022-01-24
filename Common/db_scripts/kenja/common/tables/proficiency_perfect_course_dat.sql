-- $Id: 7c39be5e4cc175c5aa3d8704702abe8c701fd1bc $

drop table PROFICIENCY_PERFECT_COURSE_DAT
create table PROFICIENCY_PERFECT_COURSE_DAT( \
    YEAR                     varchar(4)    not null, \
    SEMESTER                 varchar(1)    not null, \
    PROFICIENCYDIV           varchar(2)    not null, \
    PROFICIENCYCD            varchar(4)    not null, \
    PROFICIENCY_SUBCLASS_CD  varchar(6)    not null, \
    DIV                      varchar(2)    not null, \
    GRADE                    varchar(2)    not null, \
    COURSECD                 varchar(1)    not null, \
    MAJORCD                  varchar(3)    not null, \
    COURSECODE               varchar(4)    not null, \
    PERFECT                  smallint, \
    PASS_SCORE               smallint, \
    WEIGHTING                decimal(2,1), \
    REGISTERCD               varchar(8), \
    UPDATED                  timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_PERFECT_COURSE_DAT add constraint PK_PRO_PER_C_DAT primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, DIV, GRADE, COURSECD, MAJORCD, COURSECODE)