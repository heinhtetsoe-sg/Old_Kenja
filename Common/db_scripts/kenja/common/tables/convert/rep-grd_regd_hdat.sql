-- $Id: 1186e7894a8d061fd9b032cf6be6c4fadcc9e97a $

drop   table TMP_grd_regd_hdat
create table TMP_grd_regd_hdat \
      (year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
       grade             varchar(2)      not null, \
       hr_class          varchar(3)      not null, \
       hr_name           varchar(15), \
       hr_nameabbv       varchar(5), \
       grade_name        varchar(30), \
       hr_class_name1    varchar(30), \
       hr_class_name2    varchar(30), \
       hr_faccd          varchar(4), \
       tr_cd1            varchar(8), \
       tr_cd2            varchar(8), \
       tr_cd3            varchar(8), \
       subtr_cd1         varchar(8), \
       subtr_cd2         varchar(8), \
       subtr_cd3         varchar(8), \
       classweeks        smallint, \
       classdays         smallint, \
       registercd        varchar(8), \
       updated           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

insert into TMP_grd_regd_hdat \
  select \
       year              , \
       semester          , \
       grade             , \
       hr_class          , \
       hr_name           , \
       hr_nameabbv       , \
       grade_name, \
       hr_class_name1, \
       hr_class_name2, \
       hr_faccd          , \
       tr_cd1            , \
       tr_cd2            , \
       tr_cd3            , \
       subtr_cd1         , \
       subtr_cd2         , \
       subtr_cd3         , \
       classweeks        , \
       classdays         , \
       registercd        , \
       updated            \
  from grd_regd_hdat

drop table grd_regd_hdat_OLD

rename table     grd_regd_hdat to grd_regd_hdat_OLD

rename table TMP_grd_regd_hdat to grd_regd_hdat

alter table grd_regd_hdat add constraint pk_grd_regd_h primary key (year,semester,grade,hr_class)

