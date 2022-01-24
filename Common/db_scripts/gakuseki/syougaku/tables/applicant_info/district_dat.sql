drop table district_dat


create table district_dat \
        (setyear               varchar(4)     not null, \
         coursecd                 varchar(1)     not null, \
         majorcd                  varchar(3)     not null, \
         j_cd                     varchar(6)     not null, \
         setdistrictcd           varchar(1)     not null, \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table district_dat add constraint pk_district_dat primary key \
         (setyear, coursecd, majorcd, j_cd, setdistrictcd)
