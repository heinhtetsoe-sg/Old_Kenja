
drop table cleanattend_dat

create table cleanattend_dat \
	(cleaningday	date		not null, \
	 cleancd	varchar(4)	not null, \
	 schregno	varchar(6), \
	 di_cd		varchar(1), \
	 di_remark	varchar(50), \
	 updated	timestamp default current timestamp \
	)

alter table cleanattend_dat add constraint pk_cleanattend_dat primary key \
	(cleaningday,cleancd)
