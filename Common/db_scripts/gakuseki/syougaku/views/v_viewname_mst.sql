
drop view v_viewname_mst

create view v_viewname_mst \
	(viewyear, \
	 viewcd, \
	 viewname, \
	 showorder, \
	 updated) \
as select \
	t1.viewyear, \
	t2.viewcd, \
	t2.viewname, \
	t2.showorder, \
	t2.updated \
from 	viewnameyear_dat t1, \
	viewname_mst t2 \
where	t1.viewcd = t2.viewcd

 
