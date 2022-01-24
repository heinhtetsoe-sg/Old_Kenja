
drop table cleanact_dat

create table cleanact_dat \
	(year		varchar(4)	not null, \
	 placecd	varchar(2)	not null, \
	 cleaningday	date		not null, \
	 cleancd	varchar(4)	not null, \
	 attendcheckcd	varchar(6)	not null, \
	 attendcd	varchar(1), \
	 remark		varchar(50), \
	 updated	timestamp default current timestamp \
	)

alter table cleanact_dat add constraint pk_cleanact_dat primary key \
	(year,placecd,cleaningday,cleancd,attendcheckcd)
