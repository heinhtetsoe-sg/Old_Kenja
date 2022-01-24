
drop view v_regdyear_dat

create view v_regdyear_dat \
	(schregno, \
	 year, \
	 semester, \
	 grade, \
	 hr_class, \
         attendno, \
         coursecd, \
         majorcd, \
         coursecode1, \
         coursecode2, \
         coursecode3 \
	) \
as select \
	tbl.schregno, \
	tbl.year, \
	tbl.semester, \
	tbl.grade, \
	tbl.hr_class, \
        tbl.attendno, \
        coursecd, \
        majorcd, \
        coursecode1, \
        coursecode2, \
        coursecode3 \
from 	schreg_regd_dat tbl \
where	(tbl.schregno, tbl.year, tbl.grade,tbl.semester) in \
	( \
	 select chk.schregno, MAX(chk.year), chk.grade, chk.semester \
	 from   schreg_regd_dat chk \
	 where  chk.schregno = tbl.schregno \
	 and    chk.grade = tbl.grade \
         and    (chk.schregno, chk.year, chk.semester) in \
                ( \
                  select wk.schregno, wk.year, max(wk.semester) \
                   from schreg_regd_dat wk \
                  where wk.schregno = chk.schregno \
                  group by wk.schregno, wk.year \
                ) \      
         group by chk.schregno, chk.grade, chk.semester \
	)

