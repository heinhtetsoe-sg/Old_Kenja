
drop table nonpayment_dat

create table nonpayment_dat \
	(unpaidyear		varchar(4)	not null, \
	 unpaidmonth		varchar(2)	not null, \
	 schregno		varchar(6)	not null, \
	 statuscd		varchar(1), \
	 noneedcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table nonpayment_dat add constraint pk_nonpay_dat primary key \
	(unpaidyear, unpaidmonth, schregno)

			
