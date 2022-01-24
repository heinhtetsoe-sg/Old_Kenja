
drop view v_abroad_dat

create view v_abroad_dat \
	(year, \
	 schregno, \
	 abroadclassdays, \
	 abroadcredits) \
as select \
	char(datecnv(transfer_sdate,1), 4) as year, \
	schregno, \
	sum(abroad_classdays) as abroad_classdays, \
	sum(abroad_credits) as abroadcredits \
from 	schreg_transfer_dat \
where	transfercd = '2' \
group by schregno, datecnv(transfer_sdate, 1)


