
drop table club_disphist_dat

create table club_disphist_dat \
	(clubcd			varchar(4)	not null, \
	 dp_sdate		date		not null, \
	 dp_fdate		date, \
	 dp_tournament		varchar(40), \
	 db_place		varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table club_disphist_dat add constraint pk_clubdh_dat primary key \
	(clubcd, dp_sdate)


