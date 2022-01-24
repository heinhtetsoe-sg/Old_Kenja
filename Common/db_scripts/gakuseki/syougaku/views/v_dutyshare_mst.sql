
drop view v_dutyshare_mst

create view v_dutyshare_mst \
	(dutyshareyear, \
	 dutysharecd, \
	 sharename, \
	 updated) \
as select \
	t1.dutyshareyear, \
	t2.dutysharecd, \
	t2.sharename, \
	t2.updated \
from	dutyshareyear_dat t1, \
	dutyshare_mst t2 \
where	t1.dutysharecd = t2.dutysharecd 
