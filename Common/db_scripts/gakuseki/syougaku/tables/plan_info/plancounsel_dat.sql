
drop table plancounsel_dat

create table plancounsel_dat \
	(schregno		varchar(6)	not null, \
	 counselyear		varchar(4)	not null, \
	 counseldate		date		not null, \
	 counsel_stime		varchar(5), \
	 counsel_ftime		varchar(5), \
	 howtocounsel		varchar(20), \
	 counselor		varchar(20), \
	 staffcd		varchar(6), \
	 content		varchar(80), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table plancounsel_dat add constraint pk_counsel_dat primary key \
	(schregno, counselyear, counseldate)





