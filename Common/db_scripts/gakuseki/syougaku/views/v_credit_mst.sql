
drop view v_credit_mst

create view v_credit_mst \
	(year, \
	 coursecd, \
	 majorcd, \
	 grade, \
	 coursecode1, \
	 coursecode2, \
	 coursecode3, \
	 classcd, \
	 subclasscd, \
	 credit, \
	 require_flg, \
	 updated) \
as select \
	t1.year, \
	t1.coursecd, \
	t1.majorcd, \
	t1.grade, \
	t1.coursecode1, \
	t1.coursecode2, \
	t1.coursecode3, \
	t1.classcd, \
	t1.subclasscd, \
	t1.credit, \
	t1.require_flg, \
	t1.updated \
from	credit_mst t1

