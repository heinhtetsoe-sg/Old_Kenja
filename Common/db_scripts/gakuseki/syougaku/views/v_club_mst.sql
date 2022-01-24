
drop view v_club_mst

create view v_club_mst \
	(clubyear, \
	 clubcd, \
	 clubname, \
	 estab_date, \
	 homeground, \
	 clubroom, \
	 updated) \
as select \
	t1.clubyear, \
	t2.clubcd, \
	t2.clubname, \
	t2.estab_date, \
	t2.homeground, \
	t2.clubroom, \
	t2.updated \
from	club_year_dat t1, \
	club_mst t2 \
where	t1.clubcd = t2.clubcd


