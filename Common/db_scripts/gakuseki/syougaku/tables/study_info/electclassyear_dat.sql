
drop table electclassyear_dat

create table electclassyear_dat \
	(year		varchar(4)	not null, \
	 groupcd	smallint	not null, \
	 updated 	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table electclassyear_dat add constraint pk_ecyear_dat primary key \
	(year, groupcd)


