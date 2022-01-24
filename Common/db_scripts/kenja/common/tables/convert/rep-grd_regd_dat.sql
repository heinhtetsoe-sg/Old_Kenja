-- $Id: 98871886f5427beaa0e8ced5371e74d4f88cbb1c $

drop   table TMP_grd_regd_dat
create table TMP_grd_regd_dat \
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

insert into TMP_grd_regd_dat \
  select \
       schregno          , \
       year              , \
       semester          , \
       grade             , \
       RIGHT(RTRIM('000'||hr_class),3) as hr_class, \
       attendno          , \
       annual            , \
       seat_row          , \   
       seat_col          , \
       coursecd          , \
       majorcd           , \
       coursecode        , \
       registercd        , \
       updated            \
  from grd_regd_dat

drop table grd_regd_dat_OLD

rename table     grd_regd_dat to grd_regd_dat_OLD

rename table TMP_grd_regd_dat to grd_regd_dat

alter table grd_regd_dat add constraint pk_grd_regd primary key (schregno,year,semester)

