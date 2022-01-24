
drop table schreg_address_dat

create table schreg_address_dat \
	(schregno		varchar(6)	not null, \
	 issuedate		date		not null, \
	 expiredate		date, \
	 zipcd			varchar(8), \
	 areacd                 varchar(1), \
	 address1		varchar(50), \
	 address2 		varchar(50), \
	 address1_eng	varchar(50), \
	 address2_eng	varchar(50), \
	 telno			varchar(14), \
	 faxno			varchar(14), \
	 email			varchar(20), \
	 emergencycall		varchar(40), \
	 emergencytelno		varchar(14), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_address_dat add constraint pk_sra_dat primary key (schregno, issuedate)


