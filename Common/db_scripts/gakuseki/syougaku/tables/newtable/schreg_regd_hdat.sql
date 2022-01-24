
drop table schreg_regd_hdat

create table schreg_regd_hdat \
	(year		varchar(4)	not null, \
	 semester	varchar(1)	not null, \
	 grade		varchar(1)	not null, \
	 hr_class	varchar(2)	not null, \
	 tr_cd1		varchar(6), \
	 tr_cd2		varchar(6), \
	 tr_cd3		varchar(6), \
	 subtr_cd1	varchar(6), \
	 subtr_cd2	varchar(6), \
	 subtr_cd3	varchar(6), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms 

alter table schreg_regd_hdat add constraint pk_schregreg_hdat primary key \
	(year, semester, grade, hr_class)
