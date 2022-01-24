
drop table healthcounsel_dat

create table healthcounsel_dat \
	(schregno		varchar(6)	not null, \
	 counselyear		varchar(4)	not null, \
	 counseldate		timestamp	not null, \
	 counselsubject		varchar(2), \
	 content		varchar(50), \
	 remark			varchar(30), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table healthcounsel_dat add constraint pk_heacou_dat primary key \
	(schregno, counselyear, counseldate)


