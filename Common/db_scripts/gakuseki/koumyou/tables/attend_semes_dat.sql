
drop table attend_semes_dat

create table attend_semes_dat \
	(copycd			varchar(1)	not null, \
	 year			varchar(4) 	not null, \
	 semester		varchar(1)	not null, \
	 schregno		varchar(6)	not null, \
	 sumdate		date, \
	 classdays		smallint, \
	 absent			smallint, \
	 suspend		smallint, \
	 mourning		smallint, \
	 sick			smallint, \
	 accidentnotice		smallint, \
	 noaccidentnotice	smallint, \
	 late1			smallint, \
	 early1			smallint, \
	 crack			smallint, \
	 low_disease	smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table attend_semes_dat add constraint pk_attsemes_dat primary key \
	(copycd,year, semester, schregno)


