
drop table viewstat_dat

create table viewstat_dat \
	(year		varchar(4) 	not null, \
	 schregno	varchar(6)	not null, \
	 viewcd		varchar(4)	not null, \
	 status		varchar(1), \
	 updated	timestamp default current timestamp \
	)

alter table viewstat_dat add constraint pk_viewstat_dat primary key \
	(year,schregno,viewcd)
