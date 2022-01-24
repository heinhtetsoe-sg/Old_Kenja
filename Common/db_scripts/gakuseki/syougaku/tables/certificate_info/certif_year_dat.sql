
drop table certif_year_dat

create table certif_year_dat \
	(kindyear		varchar(4)	not null, \
	 certif_kindcd		varchar(3)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table certif_year_dat add constraint pk_certyear_dat primary key \
	(kindyear, certif_kindcd)


