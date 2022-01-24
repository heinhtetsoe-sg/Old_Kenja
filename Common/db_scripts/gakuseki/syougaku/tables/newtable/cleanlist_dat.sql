
drop table cleanlist_dat

create table cleanlist_dat \
	(year		varchar(4)	not null, \
	 cleancd	varchar(4)	not null, \
	 schregno	varchar(6)	not null, \
	 updated	timestamp default current timestamp \
	)

alter table cleanlist_dat add constraint pk_cleanlist_dat primary key \
	(year,cleancd,schregno)
