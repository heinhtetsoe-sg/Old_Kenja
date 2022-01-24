
drop table t_class_dat

create table t_class_dat \
	(year		 	varchar(4)	not null, \
	 attendclasscd		varchar(4)	not null, \
	 grade			varchar(1)	not null, \
	 hr_class		varchar(2)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table t_class_dat add constraint pk_tclass_dat primary key \
	(year, attendclasscd, grade, hr_class)


