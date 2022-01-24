drop view v_class_dat

create view v_class_dat as \
select distinct \
	'0' 								as class_kind, \
	t1.schregno							as schregno, \
	t1.lname_show || ' ' || t1.fname_show				as name, \
	t1.lkana || ' ' || t1.fkana					as kana, \
	t1.sex								as sex, \
	t2.coursecd							as coursecd, \
	t3.coursename							as coursename, \
	t2.majorcd							as majorcd, \
	t4.majorname							as majorname, \
	t2.coursecode1							as coursecode, \
	date(t2.year || '-04-01') 				as startday, \
	date(substr(char(integer(t2.year) + 1),1,4) || '-03-31')			as endday, \
	char(t2.grade) || char(t2.hr_class) 				as classcd, \
	char(t2.grade) || '年' || rtrim(char(INTEGER(t2.hr_class))) || '組'	as classalias, \
	t2.grade							as hr_grade, \
	t2.hr_class							as hr_class, \
	t2.attendno							as hr_attendno, \
	t6.tr_cd1							as tr_cd, \
	t5.lname_show || ' ' || t5.fname_show				as tr_name, \
	t2.seat_row							as seat_row, \
	t2.seat_col							as seat_col, \
	'0000'								as subclasscd, \
	'ホームルーム'							as subclassname, \
	'0'								as statuscd \
from 	schreg_base_mst t1, \
	schreg_regd_dat t2, \
	course_mst	t3, \
	major_mst	t4, \
	staff_mst	t5, \
	schreg_regd_hdat t6 \
where 	t1.schregno = t2.schregno \
  and   t2.year=t6.year \
  and   t2.semester=t6.semester \
  and   t2.grade=t6.grade \
  and   t2.hr_class=t6.hr_class \
  and 	t3.coursecd = t2.coursecd \
  and 	t4.coursecd = t2.coursecd \
  and 	t4.majorcd  = t2.majorcd \
  and 	t5.staffcd  = t6.tr_cd1 \
union select distinct \
'1'			as class_kind, \
t1.schregno		as schregno, \
t1.lname_show || ' ' || t1.fname_show	as name, \
t1.lkana || ' ' || t1.fkana	as kana, \
t1.sex			as sex, \
t2.coursecd		as coursecd, \
t3.coursename		as coursename, \
t2.majorcd		as majorcd, \
t4.majorname		as majorname, \
t2.coursecode1		as coursecode, \
MIN(t8.executedate)	as startday, \
MAX(t8.executedate)	as endday, \
t6.attendclasscd	as classcd, \
t6.classalias		as classalias, \	
t2.grade		as hr_grade, \
t2.hr_class		as hr_class, \
t2.attendno		as hr_attendno, \
t8.staffcd		as tr_cd, \
t5.lname_show || ' ' || t5.fname_show	as tr_name, \
t7.row			as seat_row, \
t7.column		as seat_col, \
t8.subclasscd		as subclasscd, \
t9.subclassname		as subclassname, \
'0' 			as statuscd \
from \
	schedule_dat 	 	t8, \
	attendclass_hdat 	t6, \
	attendclass_dat		t7, \
	schreg_base_mst		t1, \
	schreg_regd_dat		t2, \
	course_mst		t3, \
	major_mst		t4, \
	staff_mst		t5, \
	subclass_mst		t9 \
where	t6.year		 =  \
	case \
	when month(t8.executedate)<=3 \
	then char(year(t8.executedate)-1) \
	else char(year(t8.executedate)) \
	end \
  and	t6.attendclasscd = t8.attendclasscd \
  and 	t5.staffcd	 = t8.staffcd \
  and 	t9.subclasscd	 = t8.subclasscd \
  and 	t7.year		 = t6.year \
  and 	t7.attendclasscd = t6.attendclasscd \
  and 	t2.year 	 = t7.year \
  and 	t2.schregno      = t7.schregno \
  and 	t1.schregno	 = t2.schregno \
  and 	t3.coursecd	 = t2.coursecd \
  and 	t4.coursecd	 = t2.coursecd \
  and 	t4.majorcd       = t2.majorcd \
group by \
	t1.schregno, \
	t1.lname_show || ' ' || t1.fname_show, \
	t1.lkana || ' ' || t1.fkana, \
	t1.sex, \
	t2.coursecd, \
	t3.coursename, \
	t2.majorcd, \
	t4.majorname, \
	t2.coursecode1, \
	t6.attendclasscd, \
	t6.classalias, \
	t2.grade, \
	t2.hr_class, \
	t2.attendno, \
	t8.staffcd, \
	t5.lname_show || ' ' || t5.fname_show, \
	t7.row, \
	t7.column, \
	t8.subclasscd, \
	t9.subclassname 






