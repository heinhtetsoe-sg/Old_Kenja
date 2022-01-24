drop function NumOfAdmYear

CREATE FUNCTION NumOfAdmYear \
(   IN_year		varchar(4), \
    IN_semester		varchar(1), \
    IN_grade		varchar(1), \
    IN_Tkindcd		varchar(2), \
    IN_Titemcd		varchar(2) \
) RETURNS integer \
 READS SQL DATA \
 SPECIFIC NumOfAdmYear \
 LANGUAGE SQL \ 
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE Wrk_num integer default 0; \
\
  set Wrk_num = (SELECT COUNT(DISTINCT T1.schregno) FROM testscore_dat T1, schreg_regd_dat T2 \
  		 WHERE T1.year      = IN_year \
    		 AND T1.semester    = IN_semester \
    		 AND T1.testkindcd  = IN_Tkindcd \
    		 AND T1.testitemcd  = IN_Titemcd \
    		 AND T2.year        = IN_year \
    		 AND T2.grade       = IN_grade \
    		 AND T2.schregno    = T1.schregno); \
  RETURN Wrk_num; \
 \
--  IF SQLCODE < 0 THEN \
--	return 0; \
--  END IF ; \
END
