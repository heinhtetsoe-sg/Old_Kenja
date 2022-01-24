
drop view v_class_mst

create view v_class_mst \
	(classyear, \
	 classcd, \
	 classname, \
	 classabbv, \
	 classnameenglish, \
	 classabbvenglish, \ 
	 subclassnum, \
	 showorder, \
	 electdiv, \
	 updated) \
as select \
	t1.classyear, \
	t2.classcd, \
	t2.classname, \
	t2.classabbv, \
	t2.classnameenglish, \
	t2.classabbvenglish, \ 
	t2.subclasses, \
	t2.showorder, \
	t2.electdiv, \
	t2.updated \
from 	classyear_dat t1, \
	class_mst t2 \
where	t1.classcd = t2.classcd

 
