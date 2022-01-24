
drop table specialact_dat

create table specialact_dat \
	(year		varchar(4) 	not null, \
	 schregno	varchar(6)	not null, \
	 specialactcd	varchar(2)	not null, \
	 record		varchar(1), \
	 updated	timestamp default current timestamp \
	)

alter table specialact_dat add constraint pk_specialact_dat primary key \
	(year,schregno,specialactcd)
