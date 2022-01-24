
drop table doc_kind_mst

create table doc_kind_mst \
	(doc_kindcd	varchar(2)	not null, \
	 kindname	varchar(10), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table doc_kind_mst add constraint pk_dockind_mst primary key (doc_kindcd)

