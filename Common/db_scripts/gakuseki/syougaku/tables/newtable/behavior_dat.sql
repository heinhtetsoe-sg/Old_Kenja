
drop table behavior_dat

create table behavior_dat \
	(year		varchar(4) 	not null, \
	 schregno	varchar(6)	not null, \
	 behaviorcd	varchar(2)	not null, \
	 record		varchar(1), \
	 updated	timestamp default current timestamp \
	)

alter table behavior_dat add constraint pk_behavior_dat primary key \
	(year,schregno,behaviorcd)
