
drop table classweek_dat

create table classweek_dat \
	(year		varchar(4)	not null, \
	 semester	varchar(1)	not null, \
	 grade		varchar(1)	not null, \
	 hr_class	varchar(2)	not null, \
	 classweeks	smallint, \
	 classdays	smallint, \
	 di_sum_sdate	date, \
	 di_sum_fdate	date, \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table classweek_dat add constraint pk_classweek_dat primary key \
	(year, semester, grade, hr_class)




