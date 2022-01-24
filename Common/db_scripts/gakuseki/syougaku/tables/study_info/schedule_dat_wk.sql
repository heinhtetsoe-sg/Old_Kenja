
drop table schedule_dat_wk

create table schedule_dat_wk \
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
	 updated		timestamp default current timestamp \
	) in wk1dms index in wk1dms

alter table schedule_dat_wk add constraint pk_schedule_dat_wk primary key \
	(executedate, staffcd, periodcd)

 

