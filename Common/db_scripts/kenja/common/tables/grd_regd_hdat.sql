-- $Id: dc75c3c10b4e836056b2f27b30f0764b59ea3a79 $

drop table grd_regd_hdat

create table grd_regd_hdat \
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

alter table grd_regd_hdat add constraint pk_grd_regd_h primary key (year,semester,grade,hr_class)

