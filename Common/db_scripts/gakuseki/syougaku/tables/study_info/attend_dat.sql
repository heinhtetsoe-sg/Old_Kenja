
drop table attend_dat

create table attend_dat \
	(schregno		varchar(6)	not null, \
	 attenddate		date		not null, \
	 showno			smallint 	not null, \
	 periodcd		varchar(1), \
	 classcd		varchar(2), \	
	 subclasscd		varchar(4), \
	 di_cd			varchar(2), \
	 di_remark		varchar(20), \
	 updated		timestamp default current timestamp \
	)

alter table attend_dat add constraint pk_attend_dat primary key \
	(schregno, attenddate, showno)


