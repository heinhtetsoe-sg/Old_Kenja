
drop table commi_history_dat

create table commi_history_dat \
	( \
	 year	 	varchar(4)	not null, \
	 index		integer		not null, \
	 schregno	varchar(6), \
	 grade	 	varchar(1), \
	 commi_cd 	varchar(2), \
	 charge_name 	varchar(20), \
	 rolecd 	varchar(2), \
	 UPDATED	timestamp default current timestamp \
	)

alter table commi_history_dat add constraint pk_commi_history primary key \
        (year, index)
