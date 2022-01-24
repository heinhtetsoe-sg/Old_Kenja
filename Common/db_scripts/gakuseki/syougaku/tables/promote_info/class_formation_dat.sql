drop table class_formation_dat

create table class_formation_dat \
        (schregno              varchar(6) not null, \
         year                  varchar(4) not null, \
         semester	       varchar(1) not null, \
         grade                 varchar(1), \
         hr_class              varchar(2), \
         attendno              varchar(2), \
         coursecode1           varchar(3), \
         coursecode2           varchar(3), \
         coursecode3           varchar(3), \
         remaingrade_flg       varchar(1), \
         old_grade	       varchar(1), \
         old_hr_class          varchar(2), \
         old_attendno          varchar(2), \
         coursecd              varchar(1), \
         majorcd               varchar(3), \
         updated                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table class_formation_dat add constraint pk_classformation primary key (schregno,year,semester)
