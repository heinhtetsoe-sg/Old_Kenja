
drop table hreportremark_dat

create table hreportremark_dat \
	(year		varchar(4)	not null, \
	 schregno	varchar(6)	not null, \
	 studyremark	varchar(720), \
	 totalremark	varchar(1630), \
	 updated	timestamp default current timestamp \
	) in usr2dms index in idx1dms

alter table hreportremark_dat add constraint pk_hreportremark primary key \
	(year, schregno)
