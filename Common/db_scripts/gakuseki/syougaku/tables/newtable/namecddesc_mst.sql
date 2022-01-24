
drop table namecddesc_mst

create table namecddesc_mst \
	(namecd	varchar(4)	not null, \
	 cdmemo	varchar(30), \
	 updated timestamp default current timestamp \
	)  in usr1dms index in idx1dms

alter table namecddesc_mst add constraint pk_namecddesc_m primary key (namecd) 
