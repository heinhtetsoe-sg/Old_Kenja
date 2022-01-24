
drop table commi_mst

create table commi_mst \
	( \
	 commi_cd 	varchar(2)	not null, \
	 commi_name 	varchar(20), \
	 UPDATED	timestamp default current timestamp \
	)

alter table commi_mst add constraint pk_commi_mst primary key \
        (commi_cd)
