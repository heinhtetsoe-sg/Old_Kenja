
drop table hreportremark_dat

create table hreportremark_dat \
	(year		varchar(4)	not null, \
	 schregno	varchar(6)	not null, \
	 studyremark	varchar(720), \
	 totalremark	varchar(1630), \
	 study1		varchar(628), \
	 study2		varchar(628), \
	 study3		varchar(628), \
	 club1		varchar(3), \
	 club2		varchar(3), \
	 club3		varchar(3), \
	 updated	timestamp default current timestamp \
	) in usr2dms index in idx1dms

alter table hreportremark_dat add constraint pk_hreportremark primary key \
	(year, schregno)
