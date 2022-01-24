
drop table guardian_dat

create table guardian_dat \
	(schregno 		varchar(6)	not null, \
	 relationship		varchar(2)	not null, \
	 guard_lname		varchar(20), \
	 guard_fname		varchar(20), \
	 guard_lkana		varchar(40), \
	 guard_fkana		varchar(40), \
	 sex			varchar(1), \
	 birthday		date, \
	 zipcd			varchar(8), \
 	 address1		varchar(50), \
	 address2		varchar(50), \
	 telno			varchar(14), \
	 faxno			varchar(14), \
	 e_mail			varchar(20), \
	 jobcd			varchar(2), \
	 work_name		varchar(40), \
	 work_telno		varchar(14), \
	 guarantor_relationship	varchar(2), \
	 guarantor_lname	varchar(20), \
	 guarantor_fname	varchar(20), \
	 guarantor_lkana	varchar(40), \
	 guarantor_fkana	varchar(40), \
	 guarantor_sex		varchar(1), \
	 guarantor_zipcd	varchar(8), \
 	 guarantor_address1	varchar(50), \
	 guarantor_address2	varchar(50), \
	 guarantor_telno	varchar(14), \
	 guarantor_jobcd	varchar(2), \
	 public_office		varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table guardian_dat add constraint pk_guardian_dat primary key \
	(schregno)


