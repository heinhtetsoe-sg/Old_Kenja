
drop table nurseoffice_dat

create table nurseoffice_dat \
	(schregno		varchar(6)	not null, \
	 nurseyear		varchar(4)	not null, \
	 date   		timestamp	not null, \
	 date2   		timestamp, \
	 treatment_div	varchar(2), \
	 visit_reason	varchar(2), \
	 period 		varchar(2), \
	 temperature	decimal(3,1), \
	 temperature_nom	decimal(3,1), \
	 occurtimecd	varchar(2), \
	 occurtime		time, \
	 bedtime		time, \
	 risingtime		time, \
	 sleeping		varchar(2), \
	 breakfast		varchar(2), \
	 internal		varchar(2), \
	 internal2	varchar(2), \
	 internal3	varchar(2), \
	 internal4	varchar(2), \
	 internal5	varchar(2), \
	 external		varchar(2), \
	 external2	varchar(2), \
	 external3	varchar(2), \
	 external4	varchar(2), \
	 external5	varchar(2), \
	 nursetreat		varchar(2), \
	 nursetreat2	varchar(2), \
	 nursetreat3	varchar(2), \
	 nursetreat4	varchar(2), \
	 nursetreat5	varchar(2), \
	 remark     	varchar(184), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table nurseoffice_dat add constraint pk_nurse_dat primary key \
	(schregno,nurseyear,date)

insert into nurseoffice_dat \
(schregno,nurseyear,date,treatment_div,visit_reason,period,temperature,occurtimecd,occurtime,bedtime,risingtime,sleeping,breakfast,nursetreat,remark,updated) \
(select schregno,nurseyear,date,treatment_div,visit_reason,period,temperature,occurtimecd,occurtime,bedtime,risingtime,sleeping,breakfast,nursetreat,remark,updated from nurseoffice_dat_old)



