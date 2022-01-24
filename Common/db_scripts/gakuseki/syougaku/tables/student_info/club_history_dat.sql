
drop table club_history_dat

create table club_history_dat \
	(schregno		varchar(6)	not null, \
	 clubcd			varchar(4)	not null, \
	 enterdate		date		not null, \
	 quitdate		date, \
	 rolecd			varchar(2), \
	 remark			varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table club_history_dat add constraint pk_clubh_dat primary key \
	(schregno, clubcd, enterdate)




