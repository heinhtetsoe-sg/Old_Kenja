drop table class_formation_dat

create table class_formation_dat \
        (schregno               varchar(8) not null, \
         year                   varchar(4) not null, \
         semester               varchar(1) not null, \
         grade                  varchar(2), \
         hr_class               varchar(3), \
         attendno               varchar(3), \
         coursecd               varchar(1), \
         majorcd                varchar(3), \
         coursecode             varchar(4), \
         remaingrade_flg        varchar(1), \
         old_grade              varchar(2), \
         old_hr_class           varchar(3), \
         old_attendno           varchar(3), \
         registercd             varchar(8), \
         updated                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table class_formation_dat add constraint pk_classformation primary key (schregno,year,semester)
