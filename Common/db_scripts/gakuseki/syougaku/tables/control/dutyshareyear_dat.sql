
drop table dutyshareyear_dat

create table dutyshareyear_dat \
	(dutyshareyear		varchar(4)	not null, \
	 dutysharecd			varchar(4)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table dutyshareyear_dat add constraint pk_dutyyear_dat primary key \
	(dutyshareyear, dutysharecd)
