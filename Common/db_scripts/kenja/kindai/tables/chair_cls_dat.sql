drop table chair_cls_dat

create table chair_cls_dat \
      (year             varchar(4)      not null, \
       semester         varchar(1)      not null, \
       chaircd          varchar(7)      not null, \
       groupcd          varchar(4)      not null, \
       trgtgrade        varchar(2)      not null, \
       trgtclass        varchar(3)      not null, \
       registercd       varchar(8), \
       updated          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table chair_cls_dat add constraint pk_chair_cls_dat primary key \
      (year,semester,chaircd,groupcd,trgtgrade,trgtclass)
