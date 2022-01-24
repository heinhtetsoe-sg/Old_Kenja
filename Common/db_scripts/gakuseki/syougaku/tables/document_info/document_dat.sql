
drop table document_dat

create table document_dat \
	(doc_no		varchar(4)	not null, \
	 doc_index	smallint	not null, \
	 doc_year	varchar(4)	not null, \
	 doc_branch	varchar(2)	not null, \
	 doc_cd		varchar(1), \
	 doc_date	date, \
	 sender_section varchar(50), \
	 sender		varchar(50), \
	 respondsign	varchar(20), \
	 respondno	varchar(4), \
	 respondbranch	varchar(2), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table document_dat add constraint pk_doc_dat primary key \
	(doc_no, doc_index, doc_year, doc_branch)

 

