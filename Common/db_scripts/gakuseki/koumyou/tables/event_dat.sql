
drop table event_dat

create table event_dat \
	(eventdate		date	not null, \
	 s_event1		varchar(40), \
	 s_event2		varchar(40), \
	 s_event3		varchar(40), \
	 s_event4		varchar(40), \
	 s_event5		varchar(40), \
	 s_dayno1		varchar(6), \
	 s_dayno2		varchar(6), \
	 s_librarycd	varchar(1), \
	 r_event		varchar(50), \
	 r_dayno1		varchar(6), \
	 r_dayno2		varchar(6), \
	 r_librarycd	varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table event_dat add constraint pk_event_dat primary key \
	(eventdate)

-- insert into event_dat \
-- (eventdate,s_event1,s_event2,s_event3,s_event4,s_event5,s_dayno1,s_dayno2,s_librarycd,r_event,r_dayno1,r_dayno2,r_librarycd,updated) \
-- (select date(eventyear || '-' || eventmonth || '-' || eventday),substr(s_event,1,40),substr(s_event,1,40),substr(s_event,1,40),substr(s_event,1,40),substr(s_event,1,40),s_dayno1,s_dayno2,s_librarycd,r_event,r_dayno1,r_dayno2,r_librarycd,updated from event_dat_old)

