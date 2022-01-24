
drop table nurseoffice_dat

create table nurseoffice_dat \
	(schregno		varchar(6)	not null, \
	 nurseyear		varchar(4)	not null, \
	 date			timestamp	not null, \
	 weather		varchar(1), \
	 period			varchar(2), \
	 temperature		dec(3,1), \
	 internal1		varchar(2), \
	 internal2		varchar(2), \
	 internal3		varchar(2), \
	 internal4		varchar(2), \
	 internal5		varchar(2), \
	 external1		varchar(2), \
	 external2		varchar(2), \
	 external3		varchar(2), \
	 external4		varchar(2), \
	 external5		varchar(2), \
	 occurtimecd		varchar(1), \
	 occurtime		varchar(5), \
	 prev_sleephours	smallint, \
	 bedtime		smallint, \
	 risingtime		smallint, \
	 sleeping		varchar(2), \
	 breakfast		varchar(2), \
	 nursetreat1		varchar(2), \
	 nursetreat2		varchar(2), \
	 nursetreat3		varchar(2), \
	 nursetreat4		varchar(2), \
	 nursetreat5		varchar(2), \
	 remark			varchar(80), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table nurseoffice_dat add constraint pk_nurse_dat primary key \
	(schregno, nurseyear, date)




