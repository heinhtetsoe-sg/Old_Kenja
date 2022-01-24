
drop table journal_dat

create table journal_dat \
	(date		date		not null, \
	 weathercd	varchar(1), \
	 main_report	varchar(1078), \
	 year_leav	varchar(408), \
	 sick_leav	varchar(408), \
	 spcl_leav	varchar(408), \
	 occup_exept	varchar(408), \
	 business_trip	varchar(408), \
	 other		varchar(684), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table journal_dat add constraint pk_journal_dat primary key (date)
