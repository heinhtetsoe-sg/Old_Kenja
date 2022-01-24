
drop view v_course_mst

create view v_course_mst \
	(courseyear, \
	 coursecd, \
	 coursename, \
	 courseabbv, \
	 updated) \
as select \
	t1.courseyear, \
	t2.coursecd, \
	t2.coursename, \
 	t2.courseabbv, \
	t2.updated \
from \
	courseyear_dat t1, \
	course_mst t2 \
where \
	t1.coursecd = t2.coursecd 


	
