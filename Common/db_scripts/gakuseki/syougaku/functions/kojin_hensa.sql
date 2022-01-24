drop function kojin_hensa

CREATE FUNCTION KOJIN_HENSA \
( \
	in_score	decimal, \
	in_stddv	decimal, \
	in_avg		decimal \
) \
 RETURNS integer \
 SPECIFIC kojin_hensa \
 LANGUAGE SQL \
 CONTAINS SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE wk_inddv 	integer default	0; \
	set wk_inddv = ROUND((((in_score - in_avg) / in_stddv) * 10) + 50, 1) ; \
	RETURN wk_inddv; \
--EXCEPTION \
--	WHEN OTHERS THEN \
--		RETURN	0; \
END

