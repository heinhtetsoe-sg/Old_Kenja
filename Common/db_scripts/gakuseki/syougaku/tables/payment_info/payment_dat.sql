
drop table payment_dat

create table payment_dat \
	(paymentyear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 pta_exemptcd		varchar(1), \
	 tuition_exemptcd	varchar(1), \
	 deduction		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table payment_dat add constraint pk_payment_dat primary key \
	(paymentyear, schregno)


