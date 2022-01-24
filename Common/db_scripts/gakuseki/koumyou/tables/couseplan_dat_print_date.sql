--
--進路情報データにフィールド(print_date)を追加
--

-- backup
CREATE TABLE couseplan_dat_taka LIKE couseplan_dat
INSERT INTO couseplan_dat_taka SELECT * FROM couseplan_dat
-- 新テーブル作成
DROP TABLE couseplan_dat
create table couseplan_dat \
	( \
	 year	 		varchar(4)	not null, \
	 index	 		integer	not null, \
	 schregno 		varchar(6)	, \
	 stat_kind  	varchar(1), \
	 senkou_kind 	varchar(1), \
	 stat_cd    	varchar(8), \
	 stat_name  	varchar(80), \
	 buname     	varchar(80), \
	 school_sort   	varchar(1), \
	 telno      	varchar(16), \
	 howtoexam 		varchar(4), \
	 howtoexam_remark	varchar(80), \
	 hand_date 		date, \
	 decision 		varchar(4), \
	 planstat 		varchar(4), \
	 print_date     date,  \
	 senkou_no     	dec(5,0), \
	 toroku_date	date, \
	 juken_howto	varchar(4), \
	 recommend     	varchar(80), \
	 attend     	smallint, \
	 avg        	dec(2,1), \
	 test       	dec(2,0), \
	 seiseki     	dec(3,0), \
	 senkou_kai 	varchar(4), \
	 senkou_fin 	varchar(4), \
	 remark 	    varchar(40), \
	 stat_date1 	date, \
	 stat_steme 	time, \
	 stat_eteme 	time, \
	 shozoku        varchar(20), \
	 stat_date2 	date, \
	 contentexam 	varchar(80), \
	 reasonexam 	varchar(162), \
	 thinkexam 		varchar(326), \
	 job_date1 		date, \
	 job_steme 		time, \
	 job_eteme 		time, \
	 shushoku_add	varchar(80), \
	 job_remark 	varchar(80), \
	 job_content 	varchar(162), \
	 job_think 		varchar(326), \
	 jobex_date1 	date, \
	 jobex_steme 	time, \
	 jobex_eteme 	time, \
	 jobex_remark 	varchar(80), \
	 jobex_content 	varchar(162), \
	 jobex_think 	varchar(326), \
	 UPDATED	timestamp default current timestamp \
	)

alter table couseplan_dat add constraint pk_couseplan_dat primary key \
        (year, index)

-- データの移行
INSERT INTO couseplan_dat ( \
         year, \
         index, \
         schregno, \
         stat_kind, \
         senkou_kind, \
         stat_cd, \
         stat_name, \
         buname, \
         school_sort, \
         telno, \
         howtoexam, \
         howtoexam_remark, \
         hand_date, \
         decision, \
         planstat, \
         senkou_no, \
         toroku_date, \
         juken_howto, \
         recommend, \
         attend, \
         avg, \
         test, \
         seiseki, \
         senkou_kai, \
         senkou_fin, \
         remark, \
         stat_date1, \
         stat_steme, \
         stat_eteme, \
         shozoku, \
         stat_date2, \
         contentexam, \
         reasonexam, \
         thinkexam, \
         job_date1, \
         job_steme, \
         job_eteme, \
         shushoku_add, \
         job_remark, \
         job_content, \
         job_think, \
         jobex_date1, \
         jobex_steme, \
         jobex_eteme, \
         jobex_remark, \
         jobex_content, \
         jobex_think, \
         updated \
) SELECT \
         year, \
         index, \
         schregno, \
         stat_kind, \
         senkou_kind, \
         stat_cd, \
         stat_name, \
         buname, \
         school_sort, \
         telno, \
         howtoexam, \
         howtoexam_remark, \
         hand_date, \
         decision, \
         planstat, \
         senkou_no, \
         toroku_date, \
         juken_howto, \
         recommend, \
         attend, \
         avg, \
         test, \
         seiseki, \
         senkou_kai, \
         senkou_fin, \
         remark, \
         stat_date1, \
         stat_steme, \
         stat_eteme, \
         shozoku, \
         stat_date2, \
         contentexam, \
         reasonexam, \
         thinkexam, \
         job_date1, \
         job_steme, \
         job_eteme, \
         shushoku_add, \
         job_remark, \
         job_content, \
         job_think, \
         jobex_date1, \
         jobex_steme, \
         jobex_eteme, \
         jobex_remark, \
         jobex_content, \
         jobex_think, \
         updated \
	FROM couseplan_dat_taka

--
DROP TABLE couseplan_dat_taka
