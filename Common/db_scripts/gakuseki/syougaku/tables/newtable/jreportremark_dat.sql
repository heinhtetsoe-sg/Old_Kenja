
drop table jreportremark_dat

create table jreportremark_dat \
	(year		varchar(4)	not null, \
	 schregno	varchar(6) 	not null, \
	 studyremark	varchar(988), \
	 totalremark	varchar(862), \
	 attendremark1	varchar(58), \
	 attendremark2	varchar(58), \
	 attendremark3	varchar(58), \
	 club		varchar(50), \
	 role1		varchar(16), \
	 role2		varchar(16), \
	 role3		varchar(16), \
	 myrecord1	varchar(1), \
	 myrecord2	varchar(1), \
	 myrecord3	varchar(1), \
	 dayduty1	varchar(1), \
	 dayduty2	varchar(1), \
	 dayduty3	varchar(1), \
	 roleact1	varchar(1), \
	 roleact2	varchar(1), \
	 roleact3	varchar(1), \
 	 cleaning1	varchar(1), \
	 cleaning2	varchar(1), \
	 cleaning3	varchar(1), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table jreportremark_dat add constraint pk_jreportremark primary key \
	(year, schregno)


