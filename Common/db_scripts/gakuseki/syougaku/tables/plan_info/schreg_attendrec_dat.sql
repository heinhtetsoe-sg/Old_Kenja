
drop table schreg_attendrec_dat

create table schreg_attendrec_dat \
	(schoolcd		varchar(1) 	not null, \
	 schregno		varchar(6)	not null, \
	 year			varchar(4)	not null, \
	 sumdate		date, \
	 classdays		smallint, \
	 offdays		smallint, \
	 absent			smallint, \
	 suspend		smallint, \
	 mourning		smallint, \
	 abroad			smallint, \
	 requirepresent		smallint, \
	 sick			smallint, \
	 accidentnotice		smallint, \
	 noaccidentnotice	smallint, \
	 present		smallint, \
	 shrlate		smallint, \
	 late1			smallint, \
	 early1			smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_attendrec_dat add constraint pk_repatt_dat primary key \
	(schoolcd,schregno,year)


