-- $Id: 6d66f508bb90c7a4cc0b9d603a7c722e77074722 $
drop table PROFICIENCY_PERFECT_DAT

create table PROFICIENCY_PERFECT_DAT \
      (YEAR                     varchar(4) not null, \
       COURSE_DIV               varchar(1) not null, \
       GRADE                    varchar(2) not null, \
       PROFICIENCY_SUBCLASS_CD  varchar(6) not null, \
       PERFECT                  smallint not null, \
       PASS_SCORE               smallint, \
       REGISTERCD               varchar(8), \
       UPDATED                  timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table PROFICIENCY_PERFECT_DAT add constraint PK_PRO_PERFECT_D \
      primary key (YEAR, COURSE_DIV, GRADE, PROFICIENCY_SUBCLASS_CD)
