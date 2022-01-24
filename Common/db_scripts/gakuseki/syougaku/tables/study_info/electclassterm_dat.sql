
drop table electclassterm_dat

create table electclassterm_dat \
	(year		varchar(4)	not null, \
 	 groupcd	smallint	not null, \
	 group_seq	smallint	not null, \
	 startdate	date, \
	 finishdate	date, \
	 class		varchar(200), \
	 updated	timestamp default current timestamp \
	)in usr1dms index in idx1dms

alter table electclassterm_dat add constraint pk_ecterm_dat primary key \
	(year, groupcd, group_seq)


