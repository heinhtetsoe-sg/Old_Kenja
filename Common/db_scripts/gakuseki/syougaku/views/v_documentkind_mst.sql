
drop view v_documentkind_mst

create view v_documentkind_mst \
	(kindyear, \
	 doc_kindcd, \
	 kindname, \
	 updated) \
as select \
	t1.kindyear, \
	t2.doc_kindcd, \
	t2.kindname, \
	t2.updated \
from 	doc_kindyear_dat t1, \
	doc_kind_mst t2 \
where 	t1.doc_kindcd = t2.doc_kindcd
