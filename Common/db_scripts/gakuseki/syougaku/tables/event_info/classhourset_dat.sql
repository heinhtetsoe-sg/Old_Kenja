
drop table classhourset_dat

create table classhourset_dat \
	(setgrade		varchar(1)	not null, \
	 eventyear		varchar(4)	not null, \
	 eventmonth		varchar(2)	not null, \
	 eventday		varchar(2)	not null, \
	 s_classhourcd0		varchar(2), \
	 s_classhourcd1		varchar(2), \
	 s_classhourcd2		varchar(2), \
	 s_classhourcd3		varchar(2), \
	 s_classhourcd4		varchar(2), \
	 s_classhourcd5		varchar(2), \
	 s_classhourcd6		varchar(2), \
	 s_classhourcd7		varchar(2), \
	 s_daysumcd		varchar(1), \
	 s_showcd		varchar(1), \
	 r_classhourcd0		varchar(2), \
	 r_classhourcd1		varchar(2), \
	 r_classhourcd2		varchar(2), \
	 r_classhourcd3		varchar(2), \
	 r_classhourcd4		varchar(2), \
	 r_classhourcd5		varchar(2), \
	 r_classhourcd6		varchar(2), \
	 r_classhourcd7		varchar(2), \
	 r_daysumcd		varchar(1), \
	 r_showcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table classhourset_dat add constraint pk_clshourset_dat primary key \
	(setgrade, eventyear, eventmonth, eventday)


