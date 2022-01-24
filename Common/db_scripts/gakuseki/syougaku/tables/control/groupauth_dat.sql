
drop table groupauth_dat

create table groupauth_dat \
	(groupcode	varchar(4)	not null, \
	 menuid		varchar(11)	not null, \
	 groupauth	varchar(1), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table groupauth_dat add constraint pk_groupauth primary key \
	(groupcode, menuid)


