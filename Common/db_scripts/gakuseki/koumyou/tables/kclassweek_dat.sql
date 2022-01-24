
drop table kclassweek_dat

create table kclassweek_dat \
	( \
	 year 			varchar(4)	not null, \
	 grade	 		varchar(1)	not null, \
	 month	 		smallint	not null, \
	 monday 		smallint, \
	 tuesday 		smallint, \
	 wednesday 		smallint, \
	 thursday 		smallint, \
	 friday 		smallint, \
	 saturday 		smallint, \
	 sunday 		smallint, \
	 UPDATED		timestamp default current timestamp \
	)

alter table kclassweek_dat add constraint pk_kclassweek_dat primary key \
        (year, grade, month)
