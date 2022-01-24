
drop table eventremark_dat

create table eventremark_dat \
	(eventyear		varchar(4)	not null, \
	 eventmonth		varchar(2) 	not null, \
	 eventschedule		varchar(150), \
	 eventheld		varchar(150), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table eventremark_dat add constraint pk_eventrem_dat primary key \
	(eventyear, eventmonth)


