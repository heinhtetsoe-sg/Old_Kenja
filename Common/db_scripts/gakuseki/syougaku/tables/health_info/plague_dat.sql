
drop table plague_dat

create table plague_dat \
	(schregno	varchar(6)	not null, \
	 plagueyear	varchar(4)	not null, \
	 rdate		date		not null, \
	 grade		varchar(1), \
	 hr_class	varchar(2), \
	 no		varchar(2), \
	 plaguecd	varchar(2), \
	 disease	varchar(20), \
	 suspend_sdate	date, \
	 suspend_fdate	date, \
	 medreport_flg	varchar(1), \
	 plagueremark	varchar(40), \
	 updated 	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table plague_dat add constraint pk_plague_dat \
	primary key (schregno, plagueyear, rdate)


