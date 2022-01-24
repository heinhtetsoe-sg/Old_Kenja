
drop view v_job_mst

create view v_job_mst \	
	(jobyear, \
	 jobcd, \
	 jobname, \
	 updated) \
as select \
	t1.jobyear, \
	t2.jobcd, \
	t2.jobname, \
	t2.updated \
from	jobyear_dat t1, \
	job_mst t2 \
where	t1.jobcd = t2.jobcd


