
drop table disease_dat

create table disease_dat \
	(schregno		varchar(6)	not null, \
	 diseaseyear		varchar(4)	not null, \
	 occurdate		date		not null, \
	 situation		varchar(2), \
	 diseasecd		varchar(2), \
	 receive_sdate		date, \
	 receive_fdate		date, \
	 consequence		varchar(2), \
	 management		varchar(10), \
	 doc_name		varchar(10), \
	 restrict_flg		varchar(1), \
	 restriction		varchar(10), \
	 remark			varchar(15), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table disease_dat add constraint pk_disease_dat primary key \
	(schregno, diseaseyear, occurdate)


