-- $Id: 280fcda6f70fe64b66582839b22c8eda1df3b4e6 $
drop table finschool_ydat

create table finschool_ydat \
    (year           varchar(4) not null, \
     finschoolcd    varchar(12) not null, \
     registercd     varchar(10), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table finschool_ydat add constraint pk_finschool_ydat primary key \
    (year, finschoolcd)
