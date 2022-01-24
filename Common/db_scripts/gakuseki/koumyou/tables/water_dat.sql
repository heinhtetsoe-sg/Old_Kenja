--drop table water_dat

create table water_dat \
	(year			varchar(4)	not null, \
	 rezadate		date		not null, \
	 spot			varchar(2)	not null, \
	 chlorine		decimal(2,1), \
	 color			varchar(2), \
	 stink			varchar(2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table water_dat add constraint pk_water_dat primary key \
	(year,rezadate,spot)


