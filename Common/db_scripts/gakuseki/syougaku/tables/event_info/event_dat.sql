
drop table event_dat

create table event_dat \
	(eventyear		varchar(4)	not null, \
	 eventmonth		varchar(2)	not null, \
	 eventday		varchar(2)	not null, \
	 s_event		varchar(50), \
	 s_dayno1		varchar(6), \
	 s_dayno2		varchar(6), \
	 s_librarycd		varchar(1), \
	 r_event		varchar(50), \
	 r_dayno1		varchar(6), \
	 r_dayno2		varchar(6), \
	 r_librarycd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table event_dat add constraint pk_event_dat primary key \
	(eventyear, eventmonth, eventday)


