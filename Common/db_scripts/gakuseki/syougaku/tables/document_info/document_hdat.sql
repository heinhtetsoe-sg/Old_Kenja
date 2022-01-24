
drop table document_hdat

create table document_hdat \
	(doc_year	varchar(4)	not null, \
	 doc_no		varchar(4)	not null, \
	 doc_branch	varchar(2)	not null, \
	 doc_title	varchar(112), \
	 doc_kindcd	varchar(2), \
	 owner_section	varchar(12), \
	 owner		varchar(20), \
	 processcd	varchar(2), \
	 expire		date, \
	 remark		varchar(112), \
	 titlecd	varchar(1), \
	 othertitlecd	varchar(1), \
	 endcd		varchar(1), \
	 enddate	date, \
	 printcd	varchar(1), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table document_hdat add constraint pk_doc_hdat primary key \
	(doc_year, doc_no, doc_branch)


