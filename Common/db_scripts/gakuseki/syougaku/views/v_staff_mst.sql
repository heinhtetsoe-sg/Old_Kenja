
drop view v_staff_mst

create view v_staff_mst as \
select \
t1.staffyear, \
t2.staffcd, \
t1.dutysharecd, \ 
t1.jobnamecd, \
t2.lname || ' ' || fname staffname, \
t2.lname_show || ' ' || fname_show staffname_show, \
t2.lname_kana || ' ' || fname_kana staffkana, \
t2.lname_eng || ' ' || fname_eng staffeng, \
t2.staffbirthday, \
t2.staffsex, \
t2.staffzipcd, \
t2.staffaddress1, \
t2.staffaddress2, \
t2.staffhometowncd, \
t2.stafftelno, \
t1.staffsec_cd, \
t1.chargeclasscd, \
t2.updated \
from  staffyear_dat t1, staff_mst t2 \
where t1.staffcd = t2.staffcd

