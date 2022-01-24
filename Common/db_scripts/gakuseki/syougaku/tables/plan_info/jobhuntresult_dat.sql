
drop table jobhuntresult_dat

create table jobhuntresult_dat \
	(schregno		varchar(6) 	not null, \
	 receiptno		smallint	not null, \
	 receiptdate		date, \
	 decisioncd		varchar(1), \
	 univ_cd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table jobhuntresult_dat add constraint pk_jobhuntr_dat primary key \
	(schregno, receiptno)

 
