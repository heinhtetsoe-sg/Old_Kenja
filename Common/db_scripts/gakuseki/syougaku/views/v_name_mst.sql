
drop view v_name_mst

create view v_name_mst \
	(nameyear, \
	 namecd1, \
	 namecd2, \
	 name1, \
	 name2, \
 	 name3, \
	 abbv1, \
	 abbv2, \
	 abbv3, \
	 namespare1, \
	 namespare2, \
	 namespare3, \
	 updated) \
as select \
	t1.nameyear, \
	t2.namecd1, \
	t2.namecd2, \
	t2.name1, \
	t2.name2, \
	t2.name3, \
	t2.abbv1, \
	t2.abbv2, \
	t2.abbv3, \
	t2.namespare1, \
	t2.namespare2, \
	t2.namespare3, \
	t2.updated \
from	nameyear_dat t1, \
	name_mst t2 \
where	t1.namecd1 = t2.namecd1 \
and	t1.namecd2 = t2.namecd2 

