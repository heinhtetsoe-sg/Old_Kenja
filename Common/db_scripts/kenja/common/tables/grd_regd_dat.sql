-- $Id: 9f058157714eb20e88652a1143ca9a184c45542a $

drop table grd_regd_dat

create table grd_regd_dat \
      (schregno          varchar(8)      not null, \
       year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
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

alter table grd_regd_dat add constraint pk_grd_regd primary key (schregno,year,semester)

