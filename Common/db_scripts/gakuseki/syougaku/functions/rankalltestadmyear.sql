drop function RankAllTestAdmYear

CREATE FUNCTION RankAllTestAdmYear \
(   IN_schregno		varchar(6), \
    IN_year		varchar(4), \
    IN_semester		varchar(1), \
    IN_grade		varchar(1), \
    IN_TkindCD  	varchar(2), \
    IN_Titemcd		varchar(2), \
    IN_compscore	integer \
) RETURNS integer \
 READS SQL DATA \
 SPECIFIC RankAllTestAdmYear \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
    DECLARE Wrk_rank integer default 0; \
    SET Wrk_rank = (SELECT count(T1.schregno) FROM testscore_dat T1, schreg_regd_dat T2 \
          					WHERE T1.year = IN_year \
            					AND T1.semester = IN_semester \
            					AND T1.testkindcd = IN_TkindCD \
            					AND T1.testitemcd = IN_TitemCD \
            					AND T2.year       = T1.year \
            					AND T2.schregno   = T1.schregno \
            					AND T2.grade      = IN_grade \
          					GROUP BY T1.schregno \
          					HAVING SUM(T1.mod_score) > IN_compscore); \
  RETURN Wrk_rank  +  1; \
-- exception \
--    WHEN others RETURN 0; \
END

