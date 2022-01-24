-- $Id: d4fd4d61f5bbd20897d0d3289a1d296b4fec101e $

DROP TABLE chair_cls_dat_bk
CREATE TABLE chair_cls_dat_bk LIKE chair_cls_dat
insert into chair_cls_dat_bk select * from chair_cls_dat

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

insert into chair_cls_dat \
select \
        year, \
        semester, \
        chaircd, \
        groupcd, \
        trgtgrade, \
        RIGHT(RTRIM('000'||trgtclass),3) as trgtclass, \
        REGISTERCD, \
        UPDATED \
from chair_cls_dat_bk
