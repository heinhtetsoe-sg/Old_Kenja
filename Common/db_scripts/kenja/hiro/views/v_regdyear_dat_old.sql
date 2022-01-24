
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
     coursecode \
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
        coursecode \
from     schreg_regd_dat tbl \
where    (tbl.schregno, tbl.year, tbl.grade, tbl.semester) in \
    ( \
     select chk.schregno, chk.year, chk.grade, MAX(chk.semester) \
     from   schreg_regd_dat chk \
     where  chk.schregno = tbl.schregno \
     and    chk.grade = tbl.grade \
         and    (chk.schregno, chk.grade, chk.year) in \
                ( \
                  select wk.schregno, wk.grade, max(wk.year) \
                   from schreg_regd_dat wk \
                  where wk.schregno = chk.schregno \
                    and (wk.schregno,wk.year,wk.semester) in \
                        (select wk2.schregno,wk2.year,max(wk2.semester) \
                           from schreg_regd_dat wk2 \
                         group by wk2.schregno,wk2.year \
                        ) \
                  group by wk.schregno, wk.grade \
                ) \      
         group by chk.schregno, chk.year, chk.grade \
    )

