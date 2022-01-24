
drop table majoryear_dat

create table majoryear_dat \
	(majoryear	varchar(4)	not null, \
	 majorcd	varchar(3)	not null, \
	 coursecd	varchar(1)	not null, \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table majoryear_dat add constraint pk_majoryear_dat primary key \
	(majoryear, majorcd, coursecd)


