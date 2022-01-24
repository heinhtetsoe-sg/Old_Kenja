
drop table nameyear_dat 

create table nameyear_dat \
	(nameyear	varchar(4)	not null, \
	 namecd1	varchar(4)	not null, \
	 namecd2	varchar(4)	not null, \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table nameyear_dat add constraint	pk_nameyear_dat primary key \
	(nameyear, namecd1, namecd2)


