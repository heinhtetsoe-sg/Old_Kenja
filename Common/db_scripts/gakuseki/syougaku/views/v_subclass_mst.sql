
drop view v_subclass_mst

create view v_subclass_mst \
	(subclassyear, \ 
	 subclasscd, \
	 subclassname, \ 
	 subclassabbv, \
	 subclassnameenglish, \
         subclassabbvenglish, \
         showorder, \
	 electdiv, \
	 updated) \
as select \
	t1.subclassyear, \
	t2.subclasscd, \
	t2.subclassname, \
	t2.subclassabbv, \
	t2.subclassnameenglish, \
        t2.subclassabbvenglish, \
        t2.showorder, \
	t2.electdiv, \
	t2.updated \
from	subclassyear_dat t1 inner join subclass_mst t2 on t1.subclasscd = t2.subclasscd


