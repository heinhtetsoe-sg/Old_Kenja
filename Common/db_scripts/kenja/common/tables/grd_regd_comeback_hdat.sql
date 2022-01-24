-- $Id: 96671358aa24e0aa6313e89c4bdb211109731a02 $

drop table GRD_REGD_COMEBACK_HDAT

create table GRD_REGD_COMEBACK_HDAT \
      (year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
       grade             varchar(2)      not null, \
       hr_class          varchar(3)      not null, \
       COMEBACK_DATE     date            not null, \
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

alter table GRD_REGD_COMEBACK_HDAT add constraint pk_grd_regd_come primary key (YEAR, SEMESTER, GRADE, HR_CLASS, COMEBACK_DATE)

