-- $Id: finschool_ydat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table finschool_ydat

create table finschool_ydat \
    (year           varchar(4) not null, \
     finschoolcd    varchar(7) not null, \
     registercd     varchar(8), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table finschool_ydat add constraint pk_finschool_ydat primary key \
    (year, finschoolcd)
