
drop table attend_subclass_month_dat

create table attend_subclass_month_dat \
	( \
	 copycd			varchar(1)	not null, \
	 year 			varchar(4)	not null, \
	 month	 		varchar(2)	not null, \
	 schregno 		varchar(6)	not null, \
	 classcd 		varchar(2)	not null, \
	 subclasscd 	varchar(4)	not null, \
	 a_present 		smallint, \
	 a_absent 		smallint, \
	 a_suspend 		smallint, \
	 a_mourning 	smallint, \
	 a_sick 		smallint, \
	 a_notice 		smallint, \
	 a_nonotice 	smallint, \
	 a_late 		smallint, \
	 a_nurseoff 	smallint, \
	 a_late1 		smallint, \
	 a_early1 		smallint, \
	 a_crack 		smallint, \
	 a_low_disease	smallint, \
	 present 		smallint, \
	 absent 		smallint, \
	 suspend 		smallint, \
	 mourning 		smallint, \
	 sick 			smallint, \
	 notice			smallint, \
	 nonotice		smallint, \
	 late 			smallint, \
	 nurseoff 		smallint, \
	 late1 			smallint, \
	 early1 		smallint, \
	 crack  		smallint, \
	 low_disease	smallint, \
	 readchagecd 		varchar(1), \
	 UPDATED		timestamp default current timestamp \
	)

alter table attend_subclass_month_dat add constraint pk_attend_subclass primary key \
        (copycd, year, month, schregno, classcd, subclasscd)
