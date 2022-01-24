-- $Id: d6c7f021121fcfa06b9093a93b0c0cf2c838a3c8 $

drop table GRD_REGD_COMEBACK_DAT

create table GRD_REGD_COMEBACK_DAT \
      (schregno          varchar(8)      not null, \
       year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
       COMEBACK_DATE     date            not null, \
       grade             varchar(2), \
       hr_class          varchar(3), \
       attendno          varchar(3), \
       annual            varchar(2), \
       seat_row          varchar(2), \   
       seat_col          varchar(2), \
       coursecd          varchar(1), \
       majorcd           varchar(3), \
       coursecode        varchar(4), \
       registercd        varchar(8), \
       updated           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table GRD_REGD_COMEBACK_DAT add constraint pk_grd_regd_come primary key (schregno, year, semester, COMEBACK_DATE)

