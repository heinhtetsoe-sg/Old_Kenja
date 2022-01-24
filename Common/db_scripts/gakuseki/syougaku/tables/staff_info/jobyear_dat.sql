
drop table jobyear_dat

create table jobyear_dat \
	(jobyear	varchar(4)	not null, \
	 jobcd		varchar(4)	not null, \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table jobyear_dat add constraint pk_jobyear_dat primary key \
	(jobyear, jobcd)

