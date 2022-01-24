
drop table bschedule_dat

create table bschedule_dat \
	(year			varchar(4)	not null, \
	 seq			smallint	not null, \
 	 staffcd		varchar(6)	not null, \
	 daycd			varchar(1)	not null, \
	 periodcd		varchar(1)	not null, \
	 attendclasscd		varchar(4), \
	 groupcd		smallint, \
	 classcd		varchar(2), \
	 subclasscd		varchar(4), \
	 class			varchar(200), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table bschedule_dat add constraint pk_bschedule_dat primary key \
	(year, seq, staffcd, daycd, periodcd)


