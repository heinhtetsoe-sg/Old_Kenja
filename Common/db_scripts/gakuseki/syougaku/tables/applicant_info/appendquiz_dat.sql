drop table appendquiz_dat


create table appendquiz_dat \
        (appendquiznameyear                  varchar(4)     not null, \
         appendquiznamecd                    varchar(1)     not null, \
         appendquizname                      varchar(4), \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table appendquiz_dat add constraint pk_apndquz_dt primary key \
         (appendquiznameyear, appendquiznamecd)
