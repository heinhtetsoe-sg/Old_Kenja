
drop table club_year_dat

create table club_year_dat \
	(clubyear		varchar(4)	not null, \
	 clubcd			varchar(4)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table club_year_dat add constraint pk_cluby_dat primary key \
	(clubyear, clubcd)




