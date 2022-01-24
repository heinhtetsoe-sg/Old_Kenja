
drop table shifttemp_dat

create table shifttemp_dat \
	(graduateyear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 classof		varchar(3), \
	 name			varchar(40), \
	 name_show		varchar(20), \
	 name_kana		varchar(20), \
	 birthday		date, \
	 sex			varchar(1), \
	 enterdate		date, \
	 bloodtype		varchar(1), \
	 blood_rh		varchar(1), \
	 graduatedate		date, \
	 j_cd			varchar(6), \
	 coursecd		varchar(1), \
	 majorcd		varchar(3), \
	 j_graduatedate		date, \
	 guard_name		varchar(40), \
	 guard_kana		varchar(20), \
	 graduateno		varchar(5), \
	 hr_no			varchar(4), \
	 preshiftcd		varchar(2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table shifttemp_dat add constraint pk_shifttemp_dat primary key \
	(graduateyear, schregno)
