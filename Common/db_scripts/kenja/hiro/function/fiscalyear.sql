drop function FiscalYear

CREATE FUNCTION FiscalYear(in_datedata	date) \
 RETURNS varchar(4) \
 SPECIFIC FiscalYear \
 LANGUAGE SQL \
 CONTAINS SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE wk_year smallint default 1; \
	SET wk_year = INTEGER(YEAR(in_datedata)); \
	IF INTEGER(MONTH(in_datedata)) >= 1 \
	AND  INTEGER(MONTH(in_datedata))  <=  3 THEN \
		SET wk_year = wk_year - 1; \
	END IF; \
	RETURN char(char(wk_year),4); \
 END

