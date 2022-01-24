-- $Id: beddba47d3db5c25e7651ee2bda4d352ba50dfe9 $

DROP TABLE facility_mst_old

CREATE TABLE facility_mst_old LIKE facility_mst

INSERT INTO facility_mst_old SELECT * from facility_mst

DROP TABLE facility_mst

CREATE TABLE facility_mst \
    (faccd          varchar(4) not null, \
     facilityname   varchar(30), \
     facilityabbv   varchar(9), \
     capacity       smallint, \
     chr_capacity   smallint, \
     registercd     varchar(8), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE facility_mst add constraint pk_facility_mst primary key (faccd)

INSERT INTO facility_mst ( \
	faccd , \
    facilityname, \
    facilityabbv, \
    capacity, \
    chr_capacity, \
    registercd, \
    updated \
) SELECT \
	faccd , \
    facilityname, \
    facilityabbv, \
    capacity, \
    0, \
    registercd, \
    updated \
FROM facility_mst_old
