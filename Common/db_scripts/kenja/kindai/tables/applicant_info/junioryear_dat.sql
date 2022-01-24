drop table junioryear_dat


create table junioryear_dat \
        (j_year                   varchar(4)     not null, \
         j_cd                     varchar(6)     not null, \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table junioryear_dat add constraint pk_jryear_dat primary key \
         (j_year, j_cd)
