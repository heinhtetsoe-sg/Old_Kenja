
drop table cleanact_hdat

create table cleanact_hdat \
	(year		varchar(4)	not null, \
	 cleansdate	date		not null, \
	 cleanfdate	date		not null, \
	 placecd	varchar(2)	not null, \
	 attendcheckcd	varchar(6)	not null, \
	 updated	timestamp default current timestamp \
	)

alter table cleanact_hdat add constraint pk_cleanact_hdat primary key \
	(year,cleansdate,cleanfdate,placecd)
