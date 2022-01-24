
drop view v_club_regd_dat

create view v_club_regd_dat \
	(clubcd, \
	 sex, \
	 grade, \
	 result) \
as select \
	t1.clubcd, \
	t2.sex, \
	t3.grade, \
	count(t1.schregno) result \
from	(club_history_dat t1 right outer join schreg_base_mst t2 on t1.schregno = t2.schregno) \
	right outer join schreg_regd_dat t3 \
	on t1.schregno = t3.schregno \
where	t3.year = (select ctrl_char1 from control_mst where ctrl_cd1 = 'Z001') \
and	t1.quitdate IS NULL \
group by t1.clubcd, t2.sex, t3.grade


