
insert into attend_semes_dat \
select copycd, \ 
	 year, \
	 semester, \
	 schregno, \
	 sumdate, \
	 classdays, \
	 absent, \
         suspend, \
	 mourning, \
	 sick, \
	 accidentnotice, \
	 noaccidentnotice, \
	 late1, \
	 early1, \
	 updated \
from attend_semes_dat_old

