
drop table doc_kindyear_dat

create table doc_kindyear_dat \
	(kindyear	varchar(4)	not null, \
	 doc_kindcd	varchar(2)	not null, \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table doc_kindyear_dat add constraint pk_dockindyear primary key \
	(kindyear, doc_kindcd)
