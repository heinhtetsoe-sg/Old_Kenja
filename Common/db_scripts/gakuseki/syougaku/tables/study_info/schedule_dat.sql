
drop table schedule_dat

create table schedule_dat \
	(executedate		date		not null, \
	 staffcd		varchar(6)	not null, \
	 periodcd		varchar(1)	not null, \
	 groupcd		smallint, \
	 attendclasscd		varchar(4), \
	 classcd		varchar(2), \
	 subclasscd		varchar(4), \
 	 daycd			varchar(1), \
	 attendcd		varchar(1), \
	 registrarcd		varchar(6), \
	 datacd			varchar(1), \
	 year			varchar(4), \
	 semester		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schedule_dat add constraint pk_schedule_dat primary key \
	(executedate, staffcd, periodcd)

 

