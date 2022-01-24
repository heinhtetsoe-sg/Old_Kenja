
drop table userauth_dat

create table userauth_dat \
	(staffcd	varchar(6)	not null, \
	 menuid		varchar(11)	not null, \
	 userauth	varchar(1), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table userauth_dat add constraint pk_userauth_dat \
	primary key (staffcd, menuid)


