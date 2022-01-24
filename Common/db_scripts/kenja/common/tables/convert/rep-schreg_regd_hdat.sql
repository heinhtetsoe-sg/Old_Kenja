-- $Id: 05755679a5329c253fe7ff6d3694bbf922af9928 $

DROP TABLE schreg_regd_hdat_old
CREATE TABLE schreg_regd_hdat_old LIKE schreg_regd_hdat
insert into schreg_regd_hdat_old select * from schreg_regd_hdat

drop table schreg_regd_hdat
create table schreg_regd_hdat \
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

alter table schreg_regd_hdat add constraint pk_schreg_r_hdat primary key (year,semester,grade,hr_class)

insert into schreg_regd_hdat \
select \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        HR_NAME, \
        HR_NAMEABBV, \
        grade_name, \
        hr_class_name1, \
        hr_class_name2, \
        HR_FACCD, \
        TR_CD1, \
        TR_CD2, \
        TR_CD3, \
        SUBTR_CD1, \
        SUBTR_CD2, \
        SUBTR_CD3, \
        CLASSWEEKS, \
        CLASSDAYS, \
        REGISTERCD, \
        UPDATED \
from schreg_regd_hdat_old
