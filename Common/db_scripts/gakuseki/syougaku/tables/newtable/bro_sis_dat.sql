
drop table bro_sis_dat

create table bro_sis_dat \
	(schregno		varchar(6) 	not null, \
	 bro_sis_schregno	varchar(6)	not null, \
	 updated	timestamp default current timestamp \
	)

alter table bro_sis_dat add constraint pk_bro_sis_dat primary key \
	(schregno,bro_sis_schregno)
