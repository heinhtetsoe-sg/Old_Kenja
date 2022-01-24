drop function ranktestadmyear

CREATE FUNCTION RankTestAdmYear \
(   IN_schregno		varchar(6), \
    IN_year		varchar(4), \
    IN_semester		varchar(1), \
    IN_subclasscd	varchar(4), \
    IN_Tkindcd		varchar(2), \
    IN_Titemcd		varchar(2), \
    IN_compscore	integer \
) RETURNS integer \
 SPECIFIC RankTestAdmYear \
 READS SQL DATA \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
  DECLARE   Wrk_rank	integer default 0; \
  DECLARE   Wrk_grade	varchar(1); \
 \
    set wrk_grade = (SELECT grade FROM schreg_regd_dat WHERE schregno = IN_schregno AND year = IN_year); \
    set wrk_rank  = (SELECT COUNT(*) + 1 FROM testscore_dat T1, schreg_regd_dat T2 \
    		     	WHERE T1.year     = IN_year \
      			AND T1.semester   = IN_semester \
      			AND T1.subclasscd = SUBSTR(IN_subclasscd, 1, 2) \
      			AND T1.subclasscd = IN_subclasscd \
      			AND T1.testkindcd = IN_TkindCD \
      			AND T1.testitemcd = IN_TitemCD \
      			AND T1.schregno   = T2.schregno \
      			AND T1.year       = T2.year \
      			AND T2.grade      = Wrk_grade \
      			AND T1.mod_score  > IN_compscore); \
    RETURN Wrk_rank; \
--EXCEPTION \
--    WHEN OTHERS THEN RETURN 0; \
END 

