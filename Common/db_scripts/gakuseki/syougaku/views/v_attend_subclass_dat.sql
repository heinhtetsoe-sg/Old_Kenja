
drop view v_attend_subclass_dat

create view v_attend_subclass_dat \
	(year, \
	 schregno, \
	 classcd, \
	 subclasscd, \
	 a_present, \
	 a_absent, \
	 alate, \
	 a_notice, \
	 a_nonotice, \
	 present, \
	 absent, \
	 late, \
	 notice, \
	 nonotice, \
	 updated) \
as select \
	year, \
	schregno, \
	sum(integer(classcd))		classcd, \
	sum(integer(subclasscd))	subclasscd, \
	sum(a_present)			a_present, \
	sum(a_absent)			a_absent, \
	sum(a_late)			a_late, \
	sum(a_notice)			a_notice, \
	sum(a_nonotice)			a_nonotice, \
	sum(present)			present, \
	sum(absent)			absent, \	
	sum(late)			late, \
	sum(notice)			notice, \
	sum(nonotice)  			nonotice, \
	max(updated)			updated \
from attend_subclass_dat \
group by year, schregno

