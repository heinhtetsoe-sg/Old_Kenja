-- $Id: d63b0e1e08dfe2d7e2e39fda7f0d299b7e913351 $

drop table GRD_STUDYREC_DAT_OLD
create table GRD_STUDYREC_DAT_OLD like GRD_STUDYREC_DAT
insert into GRD_STUDYREC_DAT_OLD select * from GRD_STUDYREC_DAT


drop table GRD_STUDYREC_DAT

create table GRD_STUDYREC_DAT \
      (SCHOOLCD             varchar(1)      not null, \
       YEAR                 varchar(4)      not null, \
       SCHREGNO             varchar(8)      not null, \
       ANNUAL               varchar(2)      not null, \
       CLASSCD              varchar(2)      not null, \
       SUBCLASSCD           varchar(6)      not null, \
       CLASSNAME            varchar(30), \
       CLASSABBV            varchar(15), \
       CLASSNAME_ENG        varchar(40), \
       CLASSABBV_ENG        varchar(30), \
       SUBCLASSES           smallint, \
       SUBCLASSNAME         varchar(60), \
       SUBCLASSABBV         varchar(9), \
       SUBCLASSNAME_ENG     varchar(40), \
       SUBCLASSABBV_ENG     varchar(20), \
       VALUATION            smallint, \
       GET_CREDIT           smallint, \
       ADD_CREDIT           smallint, \
       COMP_CREDIT          smallint, \
       REGISTERCD           varchar(8), \
       UPDATED              timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table grd_studyrec_dat add constraint pk_grd_studyrec primary key \
      (SCHOOLCD, YEAR, SCHREGNO, ANNUAL, CLASSCD, SUBCLASSCD)

insert into GRD_STUDYREC_DAT \
  select \
         SCHOOLCD        , \
         YEAR            , \
         SCHREGNO        , \
         ANNUAL          , \
         CLASSCD         , \
         SUBCLASSCD      , \
         CLASSNAME       , \
         CLASSABBV       , \
         CLASSNAME_ENG   , \
         CLASSABBV_ENG   , \
         SUBCLASSES      , \
         SUBCLASSNAME    , \
         SUBCLASSABBV    , \
         SUBCLASSNAME_ENG, \
         SUBCLASSABBV_ENG, \
         VALUATION       , \
         GET_CREDIT      , \
         ADD_CREDIT      , \
         cast(null as SMALLINT), \
         REGISTERCD      , \
         UPDATED          \
from GRD_STUDYREC_DAT_OLD
