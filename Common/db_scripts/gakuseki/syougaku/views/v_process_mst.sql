
drop view v_process_mst

create view v_process_mst \
	(processyear, \
	 processcd, \
	 process, \
	¡õpdated) \
as select \
	t1.processyear, \
	t2.processcd, \
	t2.process, \
	t2.updated \
from	processyear_dat t1, \
	process_mst t2 \
where 	t1.processcd = t2.processcd 
