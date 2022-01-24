
drop view v_recorddetail_dat

create view v_recorddetail_dat \
	(year, \
	 semester, \
	 grade, \
	 hr_class, \
	 attendno, \
     copycd, \
 	 schregno, \
	 inoutcd, \
	 coursecd, \
	 majorcd, \
     coursecode1, \
     coursecode2, \
     coursecode3, \
	 classcd, \
	 classname, \
	 subclasses, \
	 groupcd, \
	 subclasscd, \
	 subclassname, \
	 gradingclasscd, \
	 gradingclassname, \
	 tempgrades, \
	 old_tempgrades, \
	 mod_score, \
	 minusscore, \
	 grades, \
	 credits, \
	 addcreditcd, \
	 remark, \
	 updated \
	) \
as select distinct \
	t1.year, \
	t1.semester, \
	t1.grade, \
 	t1.hr_class, \
	t1.attendno, \
    t1.copycd, \
	t1.schregno, \
    t1.inoutcd, \
	t1.coursecd, \
	t1.majorcd, \
	t1.coursecode1, \
	t1.coursecode2, \
	t1.coursecode3, \
	t1.classcd, \
	t2.classname, \
	t2.subclasses, \
	t1.groupcd, \
	t1.subclasscd, \
	t3.subclassname, \
	t1.gradingclasscd, \
	t4.subclassname as gradingclassname, \
 	t1.tempgrades, \
 	t1.old_tempgrades, \
	t1.mod_score, \
	t1.minusscore, \
	t1.grades, \
	t1.credits, \
	t1.addcreditcd, \
	t1.remark, \
	t1.updated \
from ( \
	select \
		tbl0.year, \
		tbl0.semester, \
		tbl1.grade, \
	 	tbl1.hr_class, \
		tbl1.attendno, \
	    tbl0.copycd, \
		tbl0.schregno, \
		tbl2.inoutcd, \
	 	tbl1.coursecd, \
		tbl1.majorcd, \
		tbl1.coursecode1, \
		tbl1.coursecode2, \
		tbl1.coursecode3, \
		tbl0.classcd, \
		tbl0.groupcd, \
		tbl0.subclasscd, \
		tbl0.gradingclasscd, \
		tbl0.tempgrades, \
		tbl0.old_tempgrades, \
		tbl0.mod_score, \
		tbl0.minusscore, \
		tbl0.grades, \
		tbl0.credits, \
		tbl0.addcreditcd, \
		tbl3.remark, \
		tbl0.updated \
	from	schreg_regd_dat tbl1, \
		schreg_base_mst tbl2, \
		record_dat tbl0 left join studyclassremark_dat tbl3 \
	on	tbl0.schregno = tbl3.schregno \
	and	tbl0.classcd = tbl3.classcd \
	and 	tbl0.subclasscd = tbl3.subclasscd \
	where	tbl0.classcd BETWEEN '01' AND '50' \
	and	tbl0.year = tbl1.year \
	and	tbl0.semester = tbl1.semester \
	and	tbl0.schregno = tbl1.schregno \
	and	(tbl0.schregno, tbl0.year, tbl1.grade) IN \
		( \
		 select chk.schregno, MAX(chk.year), chk.grade \
		 from	schreg_regd_dat chk \
	 	 where	chk.schregno = tbl0.schregno \
		 and 	chk.grade = tbl1.grade \
		 group by chk.schregno, chk.grade \
		) \
	and 	tbl0.schregno = tbl2.schregno \
	) t1, \
	class_mst t2, \
	subclass_mst t3, \
	subclass_mst t4 \
where	t2.classcd = t1.classcd \
and	t3.subclasscd = t1.subclasscd \
and	t4.subclasscd = t1.gradingclasscd 


