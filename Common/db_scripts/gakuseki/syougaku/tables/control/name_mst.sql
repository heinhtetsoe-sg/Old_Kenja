
drop table name_mst

create table name_mst \
	(namecd1	varchar(4)	not null, \
	 namecd2	varchar(4)	not null, \
	 name1		varchar(40), \
	 name2		varchar(40), \
	 name3		varchar(40), \
	 abbv1		varchar(20), \
	 abbv2		varchar(20), \
	 abbv3		varchar(20), \
	 namespare1	varchar(20), \
	 namespare2	varchar(20), \
	 namespare3	varchar(20), \
	 name1memo	varchar(40), \
	 name2memo	varchar(40), \
	 name3memo	varchar(40), \
	 abbv1memo	varchar(40), \
	 abbv2memo	varchar(40), \
	 abbv3memo	varchar(40), \
	 namespare1memo varchar(40), \
	 namespare2memo varchar(40), \
	 namespare3memo varchar(40), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table name_mst add constraint pk_name_mst primary key \
	(namecd1, namecd2)


