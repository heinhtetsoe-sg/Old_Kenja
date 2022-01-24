
drop table staffyear_dat

create table staffyear_dat \
	(staffyear		varchar(4)	not null, \
	 staffcd		varchar(6)	not null, \
	 dutysharecd		varchar(4), \
	 jobnamecd		varchar(4), \
	 staffsec_cd		varchar(4), \
	 requireclubcd		varchar(4), \
	 extraclubcd		varchar(4), \
	 chargeclasscd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table staffyear_dat add constraint pk_staffyear_mst_n primary key \
	(staffyear, staffcd)

-- insert into staffyear_dat \
-- (staffyear,staffcd,dutysharecd,jobnamecd,staffsec_cd,chargeclasscd) \
-- (select staffyear,staffcd,dutysharecd,jobnamecd,staffsec_cd,chargeclasscd from staffyear_dat@2)

