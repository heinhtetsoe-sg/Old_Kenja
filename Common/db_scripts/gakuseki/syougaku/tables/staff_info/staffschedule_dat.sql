
drop table staffschedule_dat

create table staffschedule_dat \
	(staffcd		varchar(6)	not null, \
	 sche_kindcd 	varchar(1)	not null, \
	 sche_sdate		date		not null, \
	 sche_fdate		date, \
	 sche_place		varchar(16), \
	 sche_note		varchar(50), \
	 exclass_stime		varchar(5), \
	 exclass_ftime		varchar(5), \
	 exclass		varchar(10), \
	 exclassgrade		varchar(3), \
	 exclassnum		smallint, \
	 exclassprogram		varchar(30), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table staffschedule_dat add constraint pk_staffsche_dat primary key \
	(staffcd, sche_kindcd, sche_sdate)

