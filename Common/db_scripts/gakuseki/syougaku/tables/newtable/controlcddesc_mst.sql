
drop table controlcddesc_mst

create table controlcddesc_mst \
	(ctrlcd	varchar(4)	not null, \
	 cdmemo	varchar(30), \
	 updated timestamp default current timestamp \
	)  in usr1dms index in idx1dms

alter table controlcddesc_mst add constraint pk_ctrlcddesc_m primary key (ctrlcd) 
