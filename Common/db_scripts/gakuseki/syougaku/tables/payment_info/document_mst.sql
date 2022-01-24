
drop table document_mst

create table document_mst \
	(documentcd		varchar(2)	not null, \
	 title			varchar(80), \
	 text			varchar(700), \
	 footnote		varchar(700), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table document_mst add constraint pk_document_dat primary key \
	(documentcd)


