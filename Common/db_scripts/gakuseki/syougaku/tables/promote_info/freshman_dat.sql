
drop table freshman_dat

create table freshman_dat \
	(enteryear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 hr_class		varchar(2), \
	 attendno 		varchar(2), \
	 inoutcd		varchar(1), \
	 coursecd		varchar(1), \
	 majorcd		varchar(3), \
	 lname			varchar(20), \
	 fname			varchar(20), \
	 lkana			varchar(40), \
	 fkana			varchar(40), \
	 birthday		date, \
	 sex			varchar(1), \
	 j_cd			varchar(6), \
	 j_graduatedate		date, \
	 zipcd			varchar(8), \
	 address1		varchar(50), \
	 address2		varchar(50), \
	 telno			varchar(14), \
	 faxno			varchar(14), \
	 email			varchar(20), \
	 emergencycall		varchar(40), \
	 emergencytelno		varchar(14), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table freshman_dat add constraint pk_freshman_dat primary key \
	(enteryear, schregno) 

