
drop table schreg_regd_dat

create table schreg_regd_dat \
        (schregno               varchar(6)      not null, \
         year                   varchar(4)      not null, \
         semester               varchar(1)      not null, \
         grade                  varchar(1), \
         hr_class               varchar(2), \
         attendno               varchar(2), \
         seat_row               varchar(2), \
         seat_col               varchar(2), \
         coursecd               varchar(1), \
         majorcd                varchar(3), \
         coursecode1            varchar(3), \
         coursecode2            varchar(3), \
         coursecode3            varchar(3), \
         updated                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table schreg_regd_dat add constraint pk_schreg_regd_dat primary key \
(schregno, year, semester)
