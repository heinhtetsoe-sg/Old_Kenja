
drop view v_setion_mst

create view v_section_mst \
	(setionyear, \
	 sectioncd,	\
	 sectionname, \
	 sectionabbv, \
	 updated) \
as select \
	t1.sectionyear, \
	t2.sectioncd, \
	t2.sectionname, \
	t2.sectionabbv, \
	t2.updated \
from 	sectionyear_dat t1, \
	section_mst t2 \
where	t1.sectioncd = t2.sectioncd

