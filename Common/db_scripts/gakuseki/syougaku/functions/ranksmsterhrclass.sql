drop function RankSmsterHRClass

CREATE FUNCTION RankSmsterHRClass \
(   IN_Mode     	integer, \
    IN_year		varchar(4), \
    IN_semester  	varchar(1), \
    IN_grade		varchar(1), \
    IN_hr_class 	varchar(2), \
    IN_subclasscd 	varchar(4), \
    IN_compscore 	integer \
) RETURNS integer \
 READS SQL DATA \
 SPECIFIC RankSmsterHRClass \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE   Wrk_rank integer default 0; \
    IF IN_MODE  =  0  THEN \		
        set wrk_rank = (SELECT COUNT(*) + 1 FROM record_dat T1, schreg_regd_dat T2 \
        		WHERE T1.year           = 	IN_year \
          		AND T1.semester         = 	IN_semester \
          		AND T1.classcd       	= 	SUBSTR(IN_subclasscd, 1, 2) \
          		AND T1.subclasscd      	= 	IN_subclasscd \
          		AND T1.grades           > 	IN_compscore \
          		AND T2.year             = 	T1.year \
          		AND T2.schregno         = 	T1.schregno \
          		AND T2.grade            = 	IN_grade \
          		AND T2.hr_class         = 	IN_hr_class); \
    ELSEIF IN_MODE  =  1  THEN \	
        set wrk_rank = (SELECT COUNT(*) + 1 FROM record_dat T1, schreg_regd_dat T2 \ 
        		WHERE T1.year         = 	IN_year \
          		AND T1.semester       = 	IN_semester \
          		AND T1.classcd        = 	SUBSTR(IN_subclassCD, 1, 2) \
          		AND T1.subclasscd     = 	IN_subclassCD \
          		AND T1.grades         > 	IN_compscore \
          		AND T2.year           = 	T1.year \
          		AND T2.schregno       = 	T1.schregno \
          		AND T2.grade          = 	IN_grade); \
    ELSEIF IN_MODE  =  2  THEN \		
       set wrk_rank = (SELECT COUNT(*) + 1 FROM (SELECT year, semester, schregno, AVG(grades) as grades \
						 FROM record_dat group by year, semester, schregno) t1, \
						 schreg_regd_dat t2 \
        		WHERE T1.year           = 	IN_year \
          		AND T1.semester         = 	IN_semester \
          		AND T1.grades           > 	IN_compscore \
          		AND T2.year             = 	T1.year \
          		AND T2.schregno         = 	T1.schregno \
          		AND T2.grade            = 	IN_grade \
          		AND T2.hr_class         = 	IN_hr_class); \
    ELSEIF IN_MODE  =  3  THEN \	 	 
       set wrk_rank = (SELECT COUNT(*) + 1 FROM (SELECT year, semester, schregno, AVG(grades) as grades \
						 FROM record_dat group by year, semester, schregno) T1, schreg_regd_dat T2 \
        		WHERE T1.year           = 	IN_year \
          		AND T1.semester         = 	IN_semester \
          		AND T1.grades           > 	IN_compscore \
          		AND T2.year             = 	T1.year \
          		AND T2.schregno         = 	T1.schregno \
          		AND T2.grade            = 	IN_grade); \
    END IF; \
    RETURN Wrk_rank; \
--EXCEPTION  \
--    WHEN OTHERS THEN RETURN 0; \
END

