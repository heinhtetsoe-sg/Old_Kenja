
drop table hoursum_dat

create table hoursum_dat \
	(yymm			varchar(7)	not null, \
	 grade			varchar(1)	not null, \
	 classweeks_s		smallint, \
	 classweeks_r		smallint, \
	 classdays_s		smallint, \
	 classdays_r		smallint, \
	 eventdays_s		smallint, \
	 eventdays_r		smallint, \
	 classhours_s		smallint, \
	 classhours_r		smallint, \
	 classmins_s		smallint, \
	 classmins_r		smallint, \
	 classhours_s_sch	smallint, \
	 classhours_r_sch	smallint, \
	 classmins_s_sch	smallint, \
	 classmins_r_sch	smallint, \
	 eventhours_s		smallint, \
	 eventhours_r		smallint, \
	 eventmins_s		smallint, \
	 eventmins_r		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table hoursum_dat add constraint pk_hoursum_dat primary key \
	(yymm, grade)


