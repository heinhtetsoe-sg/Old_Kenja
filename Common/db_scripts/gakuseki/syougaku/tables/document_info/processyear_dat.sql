
drop table processyear_dat

create table processyear_dat \
	(processyear		varchar(4)	not null, \
	 processcd		varchar(2)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table processyear_dat add constraint pk_procyear_dat primary key \
	(processyear, processcd)


