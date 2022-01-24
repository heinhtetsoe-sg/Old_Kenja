
drop view v_record_dat

create view v_record_dat \
	(year, \
	 classcd, \
	 subclasscd, \
	 schregno, \
	 groupcd, \
	 temp1, \
	 temp2, \
	 temp3, \
	 tempend, \
	 old_temp1, \
	 old_temp2, \
	 old_temp3, \
	 old_tempend, \
	 mod_1, \
	 mod_2, \
	 mod_3, \
	 mod_end, \
	 minus1, \
	 minus2, \
	 minus3, \
	 minusend, \
	 grades1, \
	 grades2, \
	 grades3, \
	 gradesend, \
	 credits, \
	 updated) \
as select \
	year, \
	classcd, \
	subclasscd, \
	schregno, \
	groupcd, \
	coalesce(sum(case semester \
			when '1' then tempgrades \
			else 0 \
		     end), 0) as temp1, \
	coalesce(sum(case semester \
                        when '2' then tempgrades \
                        else 0 \
                     end), 0) as temp2, \
       	coalesce(sum(case semester \
                        when '3' then tempgrades \
                        else 0 \
                     end), 0) as temp3, \
	coalesce(sum(case semester \
                        when '4' then tempgrades \
                        else 0 \
                     end), 0) as tempend, \
	coalesce(sum(case semester \
			when '1' then old_tempgrades \
			else 0 \
		     end), 0) as old_temp1, \
	coalesce(sum(case semester \
                        when '2' then old_tempgrades \
                        else 0 \
                     end), 0) as old_temp2, \
       	coalesce(sum(case semester \
                        when '3' then old_tempgrades \
                        else 0 \
                     end), 0) as old_temp3, \
	coalesce(sum(case semester \
                        when '4' then old_tempgrades \
                        else 0 \
                     end), 0) as old_tempend, \
         coalesce(sum(case semester \
                        when '1' then mod_score \
                        else 0 \
                     end), 0) as mod_1, \
       coalesce(sum(case semester \
                        when '2' then mod_score \
                        else 0 \
                     end), 0) as mod_2, \
       coalesce(sum(case semester \
                        when '3' then mod_score \
                        else 0 \
                     end), 0) as mod_3, \
       coalesce(sum(case semester \
                        when '4' then mod_score \
                        else 0 \
                     end), 0) as mod_end, \
       coalesce(sum(case semester \
                        when '1' then minusscore \
                        else 0 \
                     end), 0) as minus1, \
       coalesce(sum(case semester \
                        when '2' then minusscore \
                        else 0 \
                     end), 0) as minus2, \
       coalesce(sum(case semester \
                        when '3' then minusscore \
                        else 0 \
                     end), 0) as minus3, \
       coalesce(sum(case semester \
                        when '4' then minusscore \
                        else 0 \
                     end), 0) as minusend, \
       coalesce(sum(case semester \
                        when '1' then grades \
                        else 0 \
                     end), 0) as grades1, \
       coalesce(sum(case semester \
                        when '2' then grades \
                        else 0 \
                     end),0) as grades2, \
       coalesce(sum(case semester \
                        when '3' then grades \
                        else 0 \
                     end), 0) as grades3, \
       coalesce(sum(case semester \
                        when '4' then grades \
                        else 0 \
                     end), 0) as gradesend, \
	coalesce(sum(case semester \
			when '4' then grades \
			else 0 \ 
		     end), 0) as credits, \
	max(updated) as updated \
from 	record_dat \
group by \
	year, \
	classcd, \
	subclasscd, \
	schregno, \
	groupcd
