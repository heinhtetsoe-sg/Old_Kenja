
drop table schreg_transfer_dat

create table schreg_transfer_dat \
	(schregno		varchar(6)	not null, \
	 transfercd		varchar(2)	not null, \
	 transfer_sdate		date		not null, \
	 transfer_fdate		date, \
	 transferreason		varchar(50), \
	 transferplace		varchar(40), \
	 transferaddress	varchar(50), \
	 abroad_classdays	smallint, \
	 abroad_credits		smallint, \
	 updated		timestamp default current timestamp \
	)in usr1dms index in idx1dms

alter table schreg_transfer_dat add constraint pk_srt_dat primary key \
	(schregno, transfercd, transfer_sdate)




