-- $Id: 062ca2f538589f0af62fa50b5954145c67bc92e3 $

drop   table TMP_CLASS_FORMATION_DAT
create table TMP_CLASS_FORMATION_DAT \
        (SCHREGNO               varchar(8) not null, \
         YEAR                   varchar(4) not null, \
         SEMESTER               varchar(1) not null, \
         GRADE                  varchar(2), \
         HR_CLASS               varchar(3), \
         ATTENDNO               varchar(3), \
         COURSECD               varchar(1), \
         MAJORCD                varchar(3), \
         COURSECODE             varchar(4), \
         REMAINGRADE_FLG        varchar(1), \
         OLD_SCHREGNO           varchar(8), \
         OLD_GRADE              varchar(2), \
         OLD_HR_CLASS           varchar(3), \
         OLD_ATTENDNO           varchar(3), \
         SCORE                  decimal (4,1), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

insert into TMP_CLASS_FORMATION_DAT \
  select \
     SCHREGNO       , \
     YEAR           , \
     SEMESTER       , \
     GRADE          , \
     HR_CLASS       , \
     ATTENDNO       , \
     COURSECD       , \
     MAJORCD        , \
     COURSECODE     , \
     REMAINGRADE_FLG, \
     cast(null as varchar(8)) as OLD_SCHREGNO, \
     OLD_GRADE      , \
     OLD_HR_CLASS   , \
     OLD_ATTENDNO   , \
     cast(null as decimal(4,1)) as SCORE, \
     REGISTERCD     , \
     UPDATED \
  from CLASS_FORMATION_DAT

drop table CLASS_FORMATION_DAT_OLD

rename table     CLASS_FORMATION_DAT to CLASS_FORMATION_DAT_OLD

rename table TMP_CLASS_FORMATION_DAT to CLASS_FORMATION_DAT

alter table CLASS_FORMATION_DAT add constraint pk_classformation primary key (schregno,year,semester)

