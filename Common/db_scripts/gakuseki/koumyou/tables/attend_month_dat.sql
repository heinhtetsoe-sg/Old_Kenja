
drop table attend_month_dat

create table attend_month_dat \
	( \
	 copycd			varchar(1)	not null, \
	 year 			varchar(4)	not null, \
	 month	 		varchar(2)	not null, \
	 schregno 		varchar(6)	not null, \
	 sumdate 		date, \
	 classdays 		smallint, \
	 absent 		smallint, \
	 suspend 		smallint, \
	 mourning 		smallint, \
	 sick 			smallint, \
	 accidentnotice		smallint, \
	 noaccidentnotice	smallint, \
	 late1 			smallint, \
	 eary1 			smallint, \
	 crack 			smallint, \
	 low_disease	smallint, \
	 UPDATED		timestamp default current timestamp \
	)

alter table attend_month_dat add constraint pk_attend_month primary key \
        (copycd, year, month, schregno)
