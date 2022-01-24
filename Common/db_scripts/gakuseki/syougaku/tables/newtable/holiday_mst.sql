
drop table holiday_mst 

create table holiday_mst \
	(holiday 		date	not null, \
	 remark 		varchar(50), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table holiday_mst add constraint pk_holiday_mst primary key \
	(holiday)


