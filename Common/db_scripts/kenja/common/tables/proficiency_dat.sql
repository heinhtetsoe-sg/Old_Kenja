-- $Id: 7bf4b79d7e546c7fa7891e920f360f50b0bf9b2b $

drop table PROFICIENCY_DAT

create table PROFICIENCY_DAT \
    (YEAR                       varchar(4) not null, \
     SEMESTER                   varchar(1) not null, \
     PROFICIENCYDIV             varchar(2) not null, \
     PROFICIENCYCD              varchar(4) not null, \
     SCHREGNO                   varchar(8) not null, \
     PROFICIENCY_SUBCLASS_CD    varchar(6) not null, \
     GRADE                      varchar(2), \
     HR_CLASS                   varchar(3), \
     ATTENDNO                   varchar(3), \
     NAME_KANA                  varchar(120), \
     SEX                        varchar(3), \
     FORMNO                     varchar(6), \
     EXAMNO                     varchar(7), \
     SCHOOLCD                   varchar(6), \
     COURSEDIV                  varchar(3), \
     STATE_EXAM                 varchar(3), \
     EXECUTION_DAY              date, \
     SCHEDULE                   varchar(2), \
     TOTALWISHRANK              smallint, \
     TOTALWISHCNT               smallint, \
     JUDGEEVALUATION            varchar(3), \
     JUDGEVALUE                 varchar(5), \
     SUBCLASS_NAMECD            varchar(10), \
     SUBCLASS_NAME              varchar(30), \
     POINT_CONVERSION           smallint, \
     SCORE                      smallint, \
     SCORE_DI                   varchar(2), \
     DEVIATION                  decimal (4,1), \
     REMARK1                    varchar(3), \
     REMARK2                    varchar(3), \
     REMARK3                    varchar(3), \
     REMARK4                    varchar(3), \
     RANK                       smallint, \
     REGISTERCD                 varchar(8), \
     UPDATED                    timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PROFICIENCY_DAT add constraint PK_PRO_DAT primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, SCHREGNO, PROFICIENCY_SUBCLASS_CD)


