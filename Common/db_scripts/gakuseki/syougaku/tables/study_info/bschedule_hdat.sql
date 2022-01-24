
drop table bschedule_hdat

create table bschedule_hdat \
	(year			varchar(4)	not null, \
	 seq			smallint	not null, \
	 title			varchar(30), \
	 sdate			date, \
	 fdate			date, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table bschedule_hdat add constraint pk_schedule_hdat primary key \
	(year, seq)


