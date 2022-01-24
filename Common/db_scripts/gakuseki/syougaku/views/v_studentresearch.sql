
drop view v_studentresearch

create view v_studentresearch \
	(year, \
	 schregno, \
	 grade) \
as select \
	t1.year, \
	t1.schregno, \
	max(t1.grade) grade \
from 	schreg_regd_dat t1, \
	schreg_transfer_dat t2 \
where	t1.schregno = t2.schregno \
and	t2.transfercd not in ('5','8','9') \
group by t1.year, t1.schregno

 
