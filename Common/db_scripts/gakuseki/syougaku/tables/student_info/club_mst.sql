
drop table club_mst

create table club_mst \
	(clubcd			varchar(4)	not null, \
	 clubname		varchar(20), \
	 estab_date		date, \
	 homeground		varchar(20), \
	 clubroom		varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table club_mst add constraint pk_club_mst	primary key (clubcd)

 
