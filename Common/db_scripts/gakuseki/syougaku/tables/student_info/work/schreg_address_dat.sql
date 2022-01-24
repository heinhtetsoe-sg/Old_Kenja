
drop table schreg_address_dat2

create table schreg_address_dat2 \
	(schregno		varchar(6)	not null, \
	 zipcd			varchar(8), \
	 address1		varchar(50), \
	 address2 		varchar(50), \
	 telno			varchar(14) \
	) in usr1dms index in idx1dms

alter table schreg_address_dat2 add constraint pk_sra_dat2 primary key (schregno)


