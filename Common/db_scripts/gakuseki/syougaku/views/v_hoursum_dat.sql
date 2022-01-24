
drop view v_hoursum_dat

create view v_hoursum_dat \
	(year, \
	 grade, \
	 classweeks_s, \
	 classweeks_r, \
	 classdays_s, \
	 classdays_r, \
	 eventdays_s, \
	 eventdays_r, \
	 classhours_s, \
	 classhours_r, \
	 classmins_s, \
	 classmins_r, \
	 classhours_ssch, \
	 classhours_rsch, \
	 classmins_ssch, \
	 classmins_rsch, \
	 eventhours_s, \
	 eventhours_r, \
	 eventmins_s, \
	 eventmins_r, \
	 updated) \
as select \
	datecnv(date(yymm || '-01'), 1)	as year, \
	grade, \
	sum(classweeks_s)		classweeks_s, \
 	sum(classweeks_r)		classweeks_r, \
	sum(classdays_s)		classdays_s, \
	sum(classdays_r)		classdays_r, \
	sum(eventdays_s)		eventdays_s, \
	sum(eventdays_r)		eventdays_r, \
	sum(classhours_s)		classhours_s, \
	sum(classhours_r)		classhours_r, \
	sum(classmins_s)		classmins_s, \
	sum(classmins_r)		classmins_r, \
	sum(classhours_s_sch)		classhours_ssch, \
	sum(classhours_r_sch)		classhours_rsch, \
	sum(classmins_s_sch)		classmins_ssch, \
	sum(classmins_r_sch)		classmins_rsch, \
	sum(eventhours_s)		eventhours_s, \
	sum(eventhours_r)		eventhours_r, \
	sum(eventmins_s)		eventmins_s, \
	sum(eventmins_r)		eventmins_r, \
	max(updated)			updated \
from 	hoursum_dat \ 
group by \
	datecnv(date(yymm || '-01'), 1), grade

