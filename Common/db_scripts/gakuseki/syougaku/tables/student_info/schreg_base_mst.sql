
drop table schreg_base_mst

create table schreg_base_mst \
	(schregno		varchar(6)	not null, \
	 inoutcd		varchar(1), \
	 lname			varchar(20), \
	 fname			varchar(20), \
	 lname_show		varchar(10), \
	 fname_show		varchar(10), \
	 lkana			varchar(40), \
	 fkana			varchar(40), \
	 lname_eng		varchar(20), \
	 fname_eng		varchar(20), \
	 birthday		date, \
	 sex			varchar(1), \
	 bloodtype		varchar(2), \
	 blood_rh		varchar(1), \
	 j_cd			varchar(6), \
	 j_graduateddate	date, \
	 graduateno		varchar(5), \
	 bank_studentcd		varchar(6), \
         parmanentzipcd		varchar(8), \
	 parmanentaddress1	varchar(50), \
	 parmanentaddress2	varchar(50), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_base_mst add constraint pk_schreg_base_mst primary key (schregno)

