
drop table commi_year_dat

create table commi_year_dat \
	( \
	 commi_year 	varchar(4)	not null, \
	 commi_cd 	varchar(2)	not null, \
	 UPDATED	timestamp default current timestamp \
	)

alter table commi_year_dat add constraint pk_commi_year_dat primary key \
        (commi_year, commi_cd)
