
drop table grad_address_dat

create table grad_address_dat \
	(schregno	varchar(6)	not null, \
	 cur_zipcd	varchar(8), \
	 cur_areacd	varchar(1), \
	 cur_address1	varchar(50), \
	 cur_address2	varchar(50), \
	 cur_address1_eng	varchar(50), \
	 cur_address2_eng	varchar(50), \
	 cur_telno	varchar(14), \
	 cur_faxno	varchar(14), \
	 cur_email	varchar(20), \
	 cur_emergencycall	varchar(40), \
	 cur_emergencytelno	varchar(14), \
	 zipcd		varchar(8), \
	 areacd		varchar(1), \
	 address1	varchar(50), \
	 address2	varchar(50), \
	 telno		varchar(14), \
	 faxno		varchar(14), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table grad_address_dat add constraint pk_gradadd_dat primary key (schregno)


