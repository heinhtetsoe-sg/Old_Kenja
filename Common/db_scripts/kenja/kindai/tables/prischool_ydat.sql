-- $Id: prischool_ydat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table prischool_ydat

create table prischool_ydat \
    (year           varchar(4) not null, \
     prischoolcd    varchar(6) not null, \
     registercd     varchar(8), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table prischool_ydat add constraint pk_prischool_ydat primary key \
    (year, prischoolcd)
