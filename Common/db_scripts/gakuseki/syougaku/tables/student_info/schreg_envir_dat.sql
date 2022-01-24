
drop table schreg_envir_dat

create table schreg_envir_dat \
	(schregno		varchar(6)	not null, \
	 envir_year		varchar(4)	not null, \
	 license_flg		varchar(2), \
	 disease		varchar(10), \
	 healthcondition	varchar(10), \
	 merits			varchar(60), \
	 demerits		varchar(60), \
	 good_subject		varchar(60), \
	 bad_subject		varchar(60), \
	 hobby			varchar(60), \
	 readings		varchar(60), \
	 sports			varchar(60), \
	 friendship		varchar(60), \
	 specialactivity	varchar(60), \
	 howtocome		varchar(2), \
	 howtogo		varchar(2), \
	 timestocome		smallint, \
	 timestogo		smallint, \
	 residentialcd		varchar(2), \
	 updated		timestamp default current timestamp \
	)in usr1dms index in idx1dms

alter table schreg_envir_dat add constraint pk_schreg_e_dat primary key \
	(schregno, envir_year)


