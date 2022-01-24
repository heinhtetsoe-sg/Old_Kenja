
drop table jobhunt_dat

create table jobhunt_dat \
	(receiptno		smallint	not null, \
	 receiptdate		date		not null, \
	 inoutpref		varchar(1), \
	 type			varchar(10), \
	 companyname		varchar(20), \
	 companykana		varchar(10), \
	 scale			smallint, \
	 startsalary		smallint, \
	 benefitname1		varchar(12), \
	 benefit1		smallint, \
	 benefitname2		varchar(12), \
	 benefit2		smallint, \
	 benefitname3		varchar(12), \
	 benefit3		smallint, \
	 hirenum_m		smallint, \
	 hirenum_f		smallint, \
	 hirenum_n		smallint, \
	 job_m			varchar(30), \
 	 job_f			varchar(30), \
	 job_type		varchar(20), \
	 recr_zipcd		varchar(8), \
	 recr_pref		varchar(6), \
	 recr_address		varchar(50), \
	 recr_telno		varchar(14), \
	 recr_faxno		varchar(14), \
	 recr_personnel		varchar(20), \
	 recr_remark		varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table jobhunt_dat add constraint pk_jobhunt_dat primary key \
	(receiptno, receiptdate)


