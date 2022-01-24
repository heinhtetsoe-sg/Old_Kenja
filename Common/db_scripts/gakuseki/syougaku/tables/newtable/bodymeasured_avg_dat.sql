
drop table bodymeasured_avg_dat

create table bodymeasured_avg_dat \
	(year		 varchar(4)	not null, \
	 district	 varchar(1)	not null, \
	 measurement varchar(1) not null, \
	 boystudent1 decimal(4,1), \
	 boystudent2 decimal(4,1), \
	 boystudent3 decimal(4,1), \
	 girlstudent1 decimal(4,1), \
	 girlstudent2 decimal(4,1), \
	 girlstudent3 decimal(4,1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table bodymeasured_avg_dat add constraint pk_bodyavg_dat primary key (year, district, measurement)


