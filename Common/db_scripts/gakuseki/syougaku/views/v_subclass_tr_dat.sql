
drop view v_subclass_tr_dat 

create view v_subclass_tr_dat \
	(startday, \
	 endday, \
	 staffcd, \
	 staffname_show, \
	 classcd, \
	 classname, \
	 subclasscd, \
	 groupcd, \
	 group_seq, \
	 subclassname, \
	 classcode, \
	 classalias) \
as select distinct \
	min(t1.executedate) 		as startday, \
	max(t1.executedate)		as endday, \
	t1.staffcd, \
	t3.lname_show || ' ' || t3.fname_show as staffname_show, \
	t1.classcd, \
	t4.classname, \
	t1.subclasscd, \
	t1.groupcd, \
	COALESCE(t6.group_seq, 0), \
	t5.subclassname, \
	t1.attendclasscd		as classcode, \
	t2.classalias \
from  	class_mst t4, \
	staff_mst t3, \
	subclass_mst t5, \
	attendclass_hdat t2, \
	electclassstaff_dat t6 right outer join schedule_dat t1 \
	on  t6.year          = t1.year \
	and t6.attendclasscd = t1.attendclasscd \
	and t6.staffcd       = t1.staffcd \
	and t6.subclasscd    = t1.subclasscd \
	and t6.groupcd       = t1.groupcd \
where 	t2.year = t1.year \
and	t2.attendclasscd = t1.attendclasscd \
and	t3.staffcd = t1.staffcd \
and 	t4.classcd = t1.classcd \
and	t5.subclasscd = t1.subclasscd \
group by t1.staffcd, t3.lname_show, t3.fname_show, t1.classcd, t4.classname, \
	t1.subclasscd, t1.groupcd, t6.group_seq, t5.subclassname, t1.attendclasscd, t2.classalias

