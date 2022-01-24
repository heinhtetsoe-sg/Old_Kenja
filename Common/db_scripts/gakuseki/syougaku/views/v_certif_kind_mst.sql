
drop view v_certif_kind_mst

create view v_certif_kind_mst \
	(kindyear, \
	 certif_kindcd, \
	 kindname, \
	 issuecd, \
	 studentcd, \
	 graduatecd, \
	 dropoutcd, \
	 updated) \
as select \
	t1.kindyear, \
	t2.certif_kindcd, \
	t2.kindname, \
	t2.issuecd, \
	t2.studentcd, \
	t2.graduatecd, \
	t2.dropoutcd, \
	t2.updated \
from	certif_year_dat t1, \
	certif_kind_mst t2 \
where	t1.certif_kindcd = t2.certif_kindcd

