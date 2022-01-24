-- $Id: a807b188ad05bda07668ea80d67954efe9e5a9c4 $
drop table facility_mst

create table facility_mst \
    (faccd          varchar(4) not null, \
     facilityname   varchar(30), \
     facilityabbv   varchar(9), \
     capacity       smallint, \
     chr_capacity   smallint, \
     registercd     varchar(8), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table facility_mst add constraint pk_facility_mst primary key (faccd)


