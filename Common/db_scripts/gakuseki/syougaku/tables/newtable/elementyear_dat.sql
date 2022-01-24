drop table elementyear_dat


create table elementyear_dat \
        (e_year                   varchar(4)     not null, \
         e_cd                     varchar(6)     not null, \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table elementyear_dat add constraint pk_elyear_dat primary key \
         (e_year, e_cd)
