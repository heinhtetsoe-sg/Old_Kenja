
drop view v_electclass_mst

create view v_electclass_mst \
	(year, \
	 groupcd, \
	 groupname, \
	 groupabbv, \
	 remark, \
	 updated) \
as select \
	t1.year, \
	t2.groupcd, \
	t2.groupname, \
	t2.groupabbv, \
	t2.remark, \
	t2.updated \
from	electclassyear_dat t1, \
	electclass_mst t2 \
where	t1.groupcd = t2.groupcd


