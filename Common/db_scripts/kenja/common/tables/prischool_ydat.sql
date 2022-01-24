-- $Id: 57973372c08e7d496a6085f6e519ec8d147b0091 $

drop table prischool_ydat

create table prischool_ydat \
    (year           varchar(4) not null, \
     prischoolcd    varchar(7) not null, \
     registercd     varchar(8), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table prischool_ydat add constraint pk_prischool_ydat primary key \
    (year, prischoolcd)
