
drop table nurseoffice_dat

create table nurseoffice_dat \
	(schregno		varchar(6)	not null, \
	 nurseyear		varchar(4)	not null, \
	 date			timestamp	not null, \
	 treatment_div		varchar(2)      not null, \
	 visit_reason		varchar(2), \
	 period			varchar(2), \
	 temperature		decimal(3,1), \
	 occurtimecd		varchar(2), \
	 occurtime		time, \
	 bedtime		time, \
	 risingtime		time, \
	 sleeping		varchar(2), \
	 breakfast		varchar(2), \
	 nursetreat		varchar(2), \
	 remark			varchar(80), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table nurseoffice_dat add constraint pk_nurse_dat primary key \
	(schregno, nurseyear, date, treatment_div)




