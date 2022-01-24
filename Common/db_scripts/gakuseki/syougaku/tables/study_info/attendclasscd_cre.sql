
drop table attendclasscd_cre

create table attendclasscd_cre \
	(year		varchar(4)	not null, \
	 semester	varchar(1)	not null, \
	 groupcd 	smallint	not null, \
	 group_seq	smallint	not null, \
	 staffcd	varchar(6)	not null, \
	 subclasscd	varchar(4)	not null, \
	 targetclass	varchar(200)	not null, \
	 attendclasscd	varchar(4) \
	)

alter table attendclasscd_cre add constraint pk_acc_cre primary key \
	(year, semester,groupcd, group_seq, staffcd, subclasscd, targetclass)


