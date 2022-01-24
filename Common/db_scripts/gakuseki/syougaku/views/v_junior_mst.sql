
drop view v_junior_mst

create view v_junior_mst \
	(j_year, \
	 j_cd, \
	 j_name, \
	 j_kana, \
	 j_princ_name, \
	 j_princ_show, \
	 j_princ_kana, \
	 distrintcd, \
	 j_zipcd, \
	 j_address1, \
	 j_address2, \
	 j_telno, \
	 j_faxno, \
	 edboardcd, \
	 updated) \
as select \
	t1.j_year, \
	t2.j_cd, \
	t2.j_name, \
	t2.j_kana, \
	t2.j_princ_lname || ' ' || t2.j_princ_fname j_princ_name, \
	t2.j_princ_lname_show || ' ' || t2.j_princ_fname_show j_princ_show, \
	t2.j_princ_lkana || ' ' || t2.j_princ_fkana j_princ_kana, \
	t2.districtcd, \
	t2.j_zipcd, \
	t2.j_address1, \
	t2.j_address2, \
	t2.j_telno, \
	t2.j_faxno, \
	t2.edboardcd, \
	t2.updated \
from 	junioryear_dat t1, \
	junior_mst t2 \
where	t1.j_cd = t2.j_cd

