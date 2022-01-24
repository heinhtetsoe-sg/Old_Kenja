drop table chair_std_dat

create table chair_std_dat \
      (year          varchar(4)      not null, \
       semester      varchar(1)      not null, \
       chaircd       varchar(7)      not null, \
       appdate       date            not null, \
       schregno      varchar(8)      not null, \
       row           varchar(2), \
       column        varchar(2), \
       registercd    varchar(8), \
       updated       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table chair_std_dat add constraint pk_chair_std_dat primary key \
      (year,semester,chaircd,appdate,schregno)
