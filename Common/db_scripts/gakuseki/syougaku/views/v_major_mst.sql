
drop view v_major_mst

create view v_major_mst \
	(majoryear, \
	 coursecd, \
	 majorcd, \
	 majorname, \
	 majorabbv, \
	 majoreng, \
	 majorbankcd, \
	 udpated \
	) \
as select \
	t1.majoryear, \
	t2.coursecd, \
	t2.majorcd, \
	t2.majorname, \
	t2.majorabbv, \
	t2.majoreng, \
	t2.majorbankcd, \
	t2.updated \
from \
	majoryear_dat t1, \
	major_mst t2 \
where \
	t1.coursecd = t2.coursecd \
and	t1.majorcd = t2.majorcd 


