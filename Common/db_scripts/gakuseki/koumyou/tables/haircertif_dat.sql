drop table haircertif_dat 

create table haircertif_dat \
	( \
	 hairdate	date	not null, \
	 year		varchar(4)	not null, \
	 schregno	varchar(6)	not null, \
	 haircd		varchar(1) 	not null, \
	 hairreport	varchar(40), \
	 semester	varchar(1), \
	 grade		varchar(1), \
	 hr_class	varchar(2), \
	 attendno	varchar(2), \
	 tr_cd1		varchar(6), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table haircertif_dat add constraint pk_med_rir_dat primary key \
	(hairdate,year,schregno,haircd)
