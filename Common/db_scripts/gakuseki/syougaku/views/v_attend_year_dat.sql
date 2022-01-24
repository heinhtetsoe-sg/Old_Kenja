
drop view v_attend_year_dat

create view v_attend_year_dat \
	(year, \
	 schregno, \
	 present, \
	 absent, \
	 suspend, \
	 mourning, \
	 sick, \
	 accidentnotice, \
	 noaccident, \
	 late, \
	 udpated) \
as select \
	year, \
	schregno, \
	sum(present)		present, \
	sum(absent)		absent, \
	sum(suspend)		suspend, \
	sum(mourning)		mourning, \
	sum(sick)		sick, \
	sum(accidentnotice)	accidentnotice, \
	sum(noaccidentnotice)	noaccident, \
	sum(shrlate)		late, \
	max(updated)		updated \
from 	attend_semes_dat \
group by year, schregno


